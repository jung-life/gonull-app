import React from "react";
import { AbsoluteFill } from "remotion";
import qrcode from "qrcode-generator";
import { colors, fonts } from "../theme";

const PLAY_URL = "https://play.google.com/store/apps/details?id=app.gonull";

// Synchronous QR as crisp SVG (dark modules on a light card = most scannable).
const QR: React.FC<{ value: string; size: number; fg?: string; bg?: string }> = ({
  value,
  size,
  fg = colors.black,
  bg = colors.white,
}) => {
  const qr = qrcode(0, "M");
  qr.addData(value);
  qr.make();
  const count = qr.getModuleCount();
  const margin = 2; // quiet zone (modules)
  const cell = size / (count + margin * 2);
  const rects: React.ReactNode[] = [];
  for (let r = 0; r < count; r++) {
    for (let c = 0; c < count; c++) {
      if (qr.isDark(r, c)) {
        rects.push(
          <rect
            key={`${r}-${c}`}
            x={(c + margin) * cell}
            y={(r + margin) * cell}
            width={cell + 0.5}
            height={cell + 0.5}
            fill={fg}
          />
        );
      }
    }
  }
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <rect width={size} height={size} rx={0} fill={bg} />
      {rects}
    </svg>
  );
};

/**
 * Instagram carousel slides — 1080x1350 (4:5). Rendered as stills.
 * Same brand system as the app/ad: black canvas, mono headlines, green accent.
 * Story arc across the 5: hook -> why -> willpower -> pause -> CTA.
 */

const W = 1080;

// Shared chrome: brand mark top-left, progress dots top-right, consistent pad.
const SlideFrame: React.FC<{
  index: number; // 1..5
  accent?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}> = ({ index, accent = colors.green, children, footer }) => {
  return (
    <AbsoluteFill style={{ backgroundColor: colors.black, fontFamily: fonts.sans }}>
      {/* subtle top glow in the slide's accent */}
      <div
        style={{
          position: "absolute",
          top: -260,
          left: W / 2 - 260,
          width: 520,
          height: 520,
          borderRadius: "50%",
          background: accent,
          opacity: 0.14,
          filter: "blur(120px)",
        }}
      />

      <AbsoluteFill style={{ padding: 84, justifyContent: "space-between" }}>
        {/* Top row */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div style={{ fontFamily: fonts.mono, fontSize: 34, fontWeight: 700, color: colors.white }}>
            GoNull<span style={{ color: colors.green }}>_</span>
          </div>
          <div style={{ display: "flex", gap: 12 }}>
            {[1, 2, 3, 4, 5].map((n) => (
              <div
                key={n}
                style={{
                  width: n === index ? 34 : 12,
                  height: 12,
                  borderRadius: 6,
                  background: n === index ? accent : colors.border,
                }}
              />
            ))}
          </div>
        </div>

        {/* Center content */}
        <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center" }}>
          {children}
        </div>

        {/* Footer */}
        <div style={{ minHeight: 44, display: "flex", alignItems: "center" }}>
          {footer ?? (
            <div style={{ fontFamily: fonts.mono, fontSize: 30, color: colors.grayDark }}>
              swipe →
            </div>
          )}
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};

const H1: React.FC<{ children: React.ReactNode; color?: string; size?: number }> = ({
  children,
  color = colors.white,
  size = 128,
}) => (
  <div
    style={{
      fontFamily: fonts.mono,
      fontWeight: 700,
      fontSize: size,
      lineHeight: 1.02,
      letterSpacing: -1,
      color,
    }}
  >
    {children}
  </div>
);

const Body: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <div style={{ fontSize: 44, lineHeight: 1.4, color: colors.gray, marginTop: 40, maxWidth: 900 }}>
    {children}
  </div>
);

const Kicker: React.FC<{ children: React.ReactNode; color: string }> = ({ children, color }) => (
  <div
    style={{
      fontFamily: fonts.mono,
      fontSize: 30,
      letterSpacing: 6,
      fontWeight: 700,
      color,
      marginBottom: 28,
    }}
  >
    {children}
  </div>
);

// 1 — HOOK
export const Slide1: React.FC = () => (
  <SlideFrame index={1} accent={colors.red}>
    <div style={{ fontSize: 180, marginBottom: 20 }}>🎰</div>
    <H1>Your feed is a</H1>
    <H1 color={colors.red} size={150}>
      slot machine.
    </H1>
    <Body>Pull-to-refresh is the lever. You're not weak — you're outgunned.</Body>
  </SlideFrame>
);

// 2 — WHY (the design)
export const Slide2: React.FC = () => (
  <SlideFrame index={2} accent={colors.green}>
    <Kicker color={colors.green}>THE SCIENCE</Kicker>
    <H1>It's not you.</H1>
    <H1 color={colors.green}>It's the design.</H1>
    <Body>
      Endless feeds run on unpredictable rewards — the same dopamine loop that makes gambling
      addictive.
    </Body>
  </SlideFrame>
);

// 3 — WILLPOWER
export const Slide3: React.FC = () => (
  <SlideFrame index={3} accent={colors.yellow}>
    <Kicker color={colors.yellow}>WHY WILLPOWER FAILS</Kicker>
    <H1>Willpower loses</H1>
    <H1 color={colors.yellow}>in the moment.</H1>
    <Body>Your impulsive brain reacts faster than the part of you that plans ahead.</Body>
  </SlideFrame>
);

// 4 — THE PAUSE (solution)
export const Slide4: React.FC = () => (
  <SlideFrame index={4} accent={colors.green}>
    <Kicker color={colors.green}>WHAT GONULL DOES</Kicker>
    <H1>It adds a</H1>
    <H1 color={colors.green}>pause.</H1>
    <Body>
      Every attempt to open a blocked app gets harder — quick check, wait timer, reflection, then
      locked till tomorrow. The craving fades.
    </Body>
    <div style={{ display: "flex", gap: 16, marginTop: 44, flexWrap: "wrap" }}>
      {["Quick check", "5-min wait", "Reflect", "Locked"].map((t, i) => (
        <div
          key={t}
          style={{
            fontFamily: fonts.mono,
            fontSize: 30,
            fontWeight: 700,
            color: i === 3 ? colors.red : colors.white,
            border: `2px solid ${i === 3 ? colors.red : colors.border}`,
            borderRadius: 14,
            padding: "14px 22px",
          }}
        >
          {i + 1} · {t}
        </div>
      ))}
    </div>
  </SlideFrame>
);

// 5 — CTA (with QR to the Play Store + both links)
export const Slide5: React.FC = () => (
  <SlideFrame index={5} accent={colors.green} footer={<div />}>
    <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
      <div
        style={{
          fontSize: 220,
          lineHeight: 1,
          fontFamily: fonts.mono,
          fontWeight: 700,
          color: colors.green,
          textShadow: `0 0 60px ${colors.green}66`,
        }}
      >
        Ø
      </div>
      <div style={{ marginTop: 28, fontFamily: fonts.mono, fontSize: 104, fontWeight: 700 }}>
        <span style={{ color: colors.white }}>Go </span>
        <span style={{ color: colors.green }}>null.</span>
      </div>
      <div style={{ marginTop: 14, fontSize: 50, color: colors.gray }}>free your mind.</div>

      {/* QR + links */}
      <div style={{ display: "flex", alignItems: "center", gap: 40, marginTop: 72 }}>
        <div
          style={{
            background: colors.white,
            borderRadius: 22,
            padding: 22,
            border: `3px solid ${colors.green}`,
          }}
        >
          <QR value={PLAY_URL} size={240} fg={colors.black} bg={colors.white} />
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          <div style={{ fontFamily: fonts.mono, fontSize: 34, fontWeight: 700, color: colors.white }}>
            Scan to install
          </div>
          <div style={{ fontFamily: fonts.mono, fontSize: 30, color: colors.green }}>
            ▶ Google Play
          </div>
          <div style={{ fontFamily: fonts.mono, fontSize: 30, color: colors.gray }}>
            gonull.app
          </div>
        </div>
      </div>
    </div>
  </SlideFrame>
);
