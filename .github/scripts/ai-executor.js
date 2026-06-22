#!/usr/bin/env node

/**
 * LLM-agnostic executor — reads a GitHub Issue and implements the feature.
 * Swap provider via LLM_PROVIDER env var: 'claude' | 'openai' | 'gemini'
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const https = require('https');

const PROVIDER = (process.env.LLM_PROVIDER || 'claude').toLowerCase();
const ISSUE_TITLE = process.env.ISSUE_TITLE || '';
const ISSUE_BODY = process.env.ISSUE_BODY || '';

// ── Codebase snapshot ──────────────────────────────────────────────────────

function getCodebaseContext() {
  const ignored = ['node_modules', '.git', 'build', '.gradle', '*.png', '*.jpg', '*.keystore'];
  const ignoreArgs = ignored.map(i => `--exclude="${i}" --exclude-dir="${i}"`).join(' ');

  let context = '';

  // File tree
  try {
    const tree = execSync('find . -type f -name "*.kt" -o -name "*.xml" -o -name "*.gradle" -o -name "*.json" | grep -v node_modules | grep -v .git | head -80').toString();
    context += `## File Tree\n${tree}\n\n`;
  } catch (e) {}

  // CLAUDE.md for project context
  if (fs.existsSync('CLAUDE.md')) {
    context += `## Project Guide\n${fs.readFileSync('CLAUDE.md', 'utf8')}\n\n`;
  }

  // Key source files (Kotlin screens + ViewModels)
  try {
    const ktFiles = execSync('find . -name "*.kt" | grep -v node_modules | grep -v .git | head -30').toString().trim().split('\n');
    for (const file of ktFiles.slice(0, 15)) {
      if (fs.existsSync(file)) {
        const content = fs.readFileSync(file, 'utf8');
        if (content.length < 8000) {
          context += `## ${file}\n\`\`\`kotlin\n${content}\n\`\`\`\n\n`;
        }
      }
    }
  } catch (e) {}

  return context.slice(0, 80000); // stay within token limits
}

// ── LLM Providers ─────────────────────────────────────────────────────────

function callClaude(prompt) {
  return new Promise((resolve, reject) => {
    const body = JSON.stringify({
      model: 'claude-fable-5',
      max_tokens: 8192,
      messages: [{ role: 'user', content: prompt }]
    });

    const req = https.request({
      hostname: 'api.anthropic.com',
      path: '/v1/messages',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': process.env.ANTHROPIC_API_KEY,
        'anthropic-version': '2023-06-01',
        'Content-Length': Buffer.byteLength(body)
      }
    }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const parsed = JSON.parse(data);
        resolve(parsed.content?.[0]?.text || '');
      });
    });
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

function callOpenAI(prompt) {
  return new Promise((resolve, reject) => {
    const body = JSON.stringify({
      model: 'gpt-4o',
      max_tokens: 8192,
      messages: [{ role: 'user', content: prompt }]
    });

    const req = https.request({
      hostname: 'api.openai.com',
      path: '/v1/chat/completions',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`,
        'Content-Length': Buffer.byteLength(body)
      }
    }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const parsed = JSON.parse(data);
        resolve(parsed.choices?.[0]?.message?.content || '');
      });
    });
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

async function callLLM(prompt) {
  console.log(`Using provider: ${PROVIDER}`);
  switch (PROVIDER) {
    case 'openai': return callOpenAI(prompt);
    case 'claude':
    default:       return callClaude(prompt);
  }
}

// ── Main ───────────────────────────────────────────────────────────────────

async function main() {
  console.log(`Task: ${ISSUE_TITLE}`);
  console.log(`Description: ${ISSUE_BODY}`);

  const codebase = getCodebaseContext();

  const prompt = `You are an expert Android/Kotlin developer working on the GoNull app.

## Your Task
Title: ${ISSUE_TITLE}
Description: ${ISSUE_BODY}

## Codebase
${codebase}

## Instructions
1. Analyze the task and the existing codebase carefully
2. Implement the requested feature or fix following the existing patterns
3. Return ONLY a JSON array of file changes in this exact format:

\`\`\`json
[
  {
    "path": "relative/path/to/file.kt",
    "content": "full file content here"
  }
]
\`\`\`

Follow existing architecture patterns (MVVM, Room, Compose). No explanations, just the JSON.`;

  const response = await callLLM(prompt);

  // Extract JSON from response
  const jsonMatch = response.match(/```json\n([\s\S]*?)\n```/);
  if (!jsonMatch) {
    console.error('No JSON found in LLM response');
    console.error('Response was:', response.slice(0, 500));
    process.exit(1);
  }

  const changes = JSON.parse(jsonMatch[1]);

  // Apply changes
  for (const change of changes) {
    const filePath = change.path.replace(/^\//, '');
    const dir = path.dirname(filePath);
    if (dir !== '.') fs.mkdirSync(dir, { recursive: true });
    fs.writeFileSync(filePath, change.content, 'utf8');
    console.log(`Written: ${filePath}`);
  }

  console.log(`Applied ${changes.length} file change(s)`);
}

main().catch(err => {
  console.error('Executor failed:', err);
  process.exit(1);
});
