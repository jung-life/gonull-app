import React from "react";
import {
  AbsoluteFill,
  interpolate,
  spring,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { colors, fonts } from "../theme";
import { FeedScroll } from "./FeedScroll";

/**
 * "The Scroll" — 6s (180f @ 30fps), 1080x1920.
 * Doomscroll -> honest questions -> reassurance -> Go/Null/empty reveal + CTA.
 * Silent/text-first (autoplay-muted safe). Copy avoids medical claims.
 */

// A centered question line that fades in then out over its window.
const Beat: React.FC<{
  start: number;
  end: number;
  children: React.ReactNode;
}> = ({ start, end, children }) => {
  const frame = useCurrentFrame();
  const opacity = interpolate(
    frame,
    [start, start + 9, end - 9, end],
    [0, 1, 1, 0],
    { extrapolateLeft: "clamp", extrapolateRight: "clamp" }
  );
  const rise = interpolate(frame, [start, start + 12], [24, 0], {
    extrapolateRight: "clamp",
  });
  return (
    <AbsoluteFill
      style={{
        justifyContent: "center",
        alignItems: "center",
        padding: 90,
        opacity,
        transform: `translateY(${rise}px)`,
      }}
    >
      {children}
    </AbsoluteFill>
  );
};

const Reveal: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoScale = spring({
    frame: frame - 150,
    fps,
    config: { damping: 11, stiffness: 120 },
  });
  const glow = interpolate(frame, [150, 168, 180], [0, 26, 18], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const wordOpacity = interpolate(frame, [158, 168], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const tagOpacity = interpolate(frame, [166, 176], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const ctaOpacity = interpolate(frame, [172, 180], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        justifyContent: "center",
        alignItems: "center",
        padding: 80,
      }}
    >
      <div
        style={{
          fontSize: 240,
          lineHeight: 1,
          color: colors.green,
          fontWeight: 700,
          fontFamily: fonts.mono,
          textShadow: `0 0 ${glow}px ${colors.green}`,
          transform: `scale(${logoScale})`,
        }}
      >
        Ø
      </div>

      <div
        style={{
          marginTop: 44,
          fontSize: 96,
          fontWeight: 700,
          fontFamily: fonts.mono,
          opacity: wordOpacity,
        }}
      >
        <span style={{ color: colors.white }}>Go </span>
        <span style={{ color: colors.green }}>null.</span>
      </div>

      <div
        style={{
          marginTop: 20,
          fontSize: 46,
          color: colors.gray,
          fontFamily: fonts.sans,
          opacity: tagOpacity,
        }}
      >
        empty your mind.
      </div>

      <div
        style={{
          marginTop: 64,
          fontSize: 34,
          color: colors.green,
          fontFamily: fonts.mono,
          opacity: ctaOpacity,
        }}
      >
        Free on Google Play · gonull.app
      </div>
    </AbsoluteFill>
  );
};

export const DoomscrollAd: React.FC = () => {
  const frame = useCurrentFrame();

  // Feed dims as the interrupt begins.
  const dim = interpolate(frame, [58, 96], [0, 0.66], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Crossfade the feed+questions layer out into the reveal.
  const feedOpacity = interpolate(frame, [146, 156], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill style={{ backgroundColor: colors.black }}>
      {/* Layer 1: feed + questions */}
      <AbsoluteFill style={{ opacity: feedOpacity }}>
        <FeedScroll dim={dim} />

        <Beat start={68} end={98}>
          <div
            style={{
              fontSize: 88,
              fontWeight: 700,
              color: colors.white,
              fontFamily: fonts.mono,
              textAlign: "center",
            }}
          >
            Still scrolling?
          </div>
        </Beat>

        <Beat start={98} end={128}>
          <div
            style={{
              fontSize: 76,
              fontWeight: 700,
              color: colors.white,
              fontFamily: fonts.mono,
              textAlign: "center",
              lineHeight: 1.2,
            }}
          >
            And it's still
            <br />
            not enough.
          </div>
        </Beat>

        <Beat start={128} end={150}>
          <div style={{ textAlign: "center", fontFamily: fonts.mono, fontWeight: 700, lineHeight: 1.25 }}>
            <div style={{ fontSize: 72, color: colors.white }}>It's not you.</div>
            <div style={{ fontSize: 72, color: colors.green, marginTop: 8 }}>
              It's designed that way.
            </div>
          </div>
        </Beat>
      </AbsoluteFill>

      {/* Layer 2: reveal + CTA */}
      {frame >= 146 && <Reveal />}
    </AbsoluteFill>
  );
};
