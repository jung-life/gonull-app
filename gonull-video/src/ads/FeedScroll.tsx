import React from "react";
import {
  AbsoluteFill,
  interpolate,
  useCurrentFrame,
  OffthreadVideo,
  Easing,
} from "remotion";
import { colors, fonts } from "../theme";

/**
 * A stylized, abstract "doomscroll" feed. Deliberately NOT a clone of any real
 * app — generic skeleton cards in brand colors, no real logos/handles/faces.
 *
 * Hybrid slot: pass `backgroundVideoSrc` to composite a real screen-recording
 * behind the overlay instead of the stylized column (footage path). Everything
 * else in the ad stays identical.
 */

type Post = {
  tone: string; // image-block tint
  handle: string;
  lines: number; // caption bars
  likes: string;
  reposts: string;
  comments: string;
};

// Deterministic, non-branded sample posts (varied so the scroll feels alive).
const POSTS: Post[] = [
  { tone: colors.surface, handle: "@daily_mix_88", lines: 2, likes: "2.1k", reposts: "88", comments: "340" },
  { tone: "#1c2b1e", handle: "@for.you.page", lines: 1, likes: "14.7k", reposts: "1.2k", comments: "903" },
  { tone: "#2b1c1c", handle: "@trend_now", lines: 3, likes: "512", reposts: "40", comments: "77" },
  { tone: colors.surface, handle: "@scroll.hole", lines: 2, likes: "8.9k", reposts: "610", comments: "455" },
  { tone: "#2a271a", handle: "@one_more_video", lines: 1, likes: "33.2k", reposts: "4.1k", comments: "2.6k" },
  { tone: colors.surface, handle: "@late_night_feed", lines: 2, likes: "1.4k", reposts: "52", comments: "119" },
  { tone: "#1c2b1e", handle: "@algo_says_hi", lines: 3, likes: "6.0k", reposts: "300", comments: "288" },
  { tone: "#2b1c1c", handle: "@just_5_min", lines: 1, likes: "22.5k", reposts: "1.9k", comments: "1.1k" },
  { tone: colors.surface, handle: "@keep_going", lines: 2, likes: "740", reposts: "31", comments: "64" },
  { tone: "#2a271a", handle: "@infinite_scroll", lines: 2, likes: "11.8k", reposts: "980", comments: "705" },
];

const Bar: React.FC<{ w: number | string; h?: number; c?: string; mt?: number }> = ({
  w,
  h = 16,
  c = colors.border,
  mt = 0,
}) => (
  <div style={{ width: w, height: h, marginTop: mt, backgroundColor: c, borderRadius: h / 2 }} />
);

const PostCard: React.FC<{ post: Post }> = ({ post }) => (
  <div style={{ marginBottom: 34 }}>
    {/* header: avatar + handle */}
    <div style={{ display: "flex", alignItems: "center", gap: 18, marginBottom: 20 }}>
      <div style={{ width: 74, height: 74, borderRadius: 37, backgroundColor: colors.border }} />
      <div style={{ color: colors.grayDark, fontSize: 30, fontFamily: fonts.mono }}>
        {post.handle}
      </div>
      <div style={{ marginLeft: "auto", color: colors.grayDark, fontSize: 40, letterSpacing: 3 }}>
        •••
      </div>
    </div>

    {/* media block */}
    <div
      style={{
        width: "100%",
        height: 560,
        borderRadius: 24,
        backgroundColor: post.tone,
        border: `1px solid ${colors.border}`,
      }}
    />

    {/* engagement row */}
    <div style={{ display: "flex", alignItems: "center", gap: 46, marginTop: 22, color: colors.gray, fontFamily: fonts.sans, fontSize: 30 }}>
      <span>♡ {post.likes}</span>
      <span>↺ {post.reposts}</span>
      <span>💬 {post.comments}</span>
    </div>

    {/* caption bars */}
    {Array.from({ length: post.lines }).map((_, i) => (
      <Bar key={i} w={i === post.lines - 1 ? "55%" : "88%"} c={colors.surface} mt={i === 0 ? 22 : 14} />
    ))}
  </div>
);

export const FeedScroll: React.FC<{
  backgroundVideoSrc?: string;
  dim?: number; // 0..1 dark overlay
  videoPlaybackRate?: number; // slow real footage for a more hypnotic scroll
  videoBlur?: number; // px blur — obscures identifiable faces/logos/content
}> = ({ backgroundVideoSrc, dim = 0, videoPlaybackRate = 0.7, videoBlur = 0 }) => {
  const frame = useCurrentFrame();

  // Hypnotic auto-scroll: accelerates (lost time), then decelerates as the
  // questions arrive and the feed dims.
  const scrollY = interpolate(frame, [0, 66, 100], [0, -1650, -1850], {
    extrapolateRight: "clamp",
    easing: Easing.inOut(Easing.quad),
  });

  return (
    <AbsoluteFill style={{ backgroundColor: colors.black, overflow: "hidden" }}>
      {backgroundVideoSrc ? (
        <OffthreadVideo
          src={backgroundVideoSrc}
          muted
          playbackRate={videoPlaybackRate}
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
            // scale up so the heavier blur doesn't reveal the frame edges
            transform: videoBlur ? "scale(1.25)" : undefined,
            filter: videoBlur ? `blur(${videoBlur}px)` : undefined,
          }}
        />
      ) : (
        <div style={{ transform: `translateY(${scrollY}px)`, padding: "56px 56px 0" }}>
          {POSTS.map((p, i) => (
            <PostCard key={i} post={p} />
          ))}
        </div>
      )}

      {/* darkening overlay as the interrupt begins */}
      <AbsoluteFill style={{ backgroundColor: `rgba(10,10,10,${dim})` }} />
    </AbsoluteFill>
  );
};
