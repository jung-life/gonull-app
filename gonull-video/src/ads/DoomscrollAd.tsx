import React from "react";
import {
  AbsoluteFill,
  Audio,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { colors, fonts } from "../theme";
import { FeedScroll } from "./FeedScroll";

/**
 * "The Scroll" — vertical doomscroll-interrupt ad, 1080x1920, 360f @ 30fps (12s).
 * Doomscroll (real feed footage) -> honest questions -> reassurance ->
 * Go/null/"free your mind" reveal + CTA. Music from public/song.mp4.
 *
 * Pacing is deliberately unhurried: each text beat holds ~2s, the reveal ~2.5s.
 * Copy avoids medical claims per Terms ("designed that way", not "cure").
 */

// Media lives in public/ (served by Remotion via staticFile).
const FEED_SRC = staticFile("feed.mp4");
const SONG_SRC = staticFile("song.mp4");

// A centered text beat that fades in, holds, then fades out over its window.
const Beat: React.FC<{ start: number; end: number; children: React.ReactNode }> = ({
  start,
  end,
  children,
}) => {
  const frame = useCurrentFrame();
  const opacity = interpolate(
    frame,
    [start, start + 12, end - 12, end],
    [0, 1, 1, 0],
    { extrapolateLeft: "clamp", extrapolateRight: "clamp" }
  );
  const rise = interpolate(frame, [start, start + 16], [26, 0], {
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
    frame: frame - 285,
    fps,
    config: { damping: 12, stiffness: 110 },
  });
  const glow = interpolate(frame, [285, 320, 360], [0, 26, 20], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const wordOpacity = interpolate(frame, [300, 315], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const tagOpacity = interpolate(frame, [315, 330], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const ctaOpacity = interpolate(frame, [332, 348], [0, 1], {
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
        free your mind.
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

  // Feed stays clear for the first ~3s, then dims as the interrupt begins.
  const dim = interpolate(frame, [90, 130], [0, 0.66], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Crossfade the feed+questions layer out into the reveal.
  const feedOpacity = interpolate(frame, [280, 298], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Music: fade in at the start, fade out under the end card.
  const musicVolume = (f: number) =>
    interpolate(f, [0, 18, 336, 360], [0, 0.85, 0.85, 0], {
      extrapolateLeft: "clamp",
      extrapolateRight: "clamp",
    });

  return (
    <AbsoluteFill style={{ backgroundColor: colors.black }}>
      <Audio src={SONG_SRC} volume={musicVolume} />

      {/* Layer 1: real feed footage + questions */}
      <AbsoluteFill style={{ opacity: feedOpacity }}>
        <FeedScroll backgroundVideoSrc={FEED_SRC} dim={dim} videoPlaybackRate={0.55} />

        <Beat start={100} end={165}>
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

        <Beat start={165} end={230}>
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

        <Beat start={230} end={290}>
          <div style={{ textAlign: "center", fontFamily: fonts.mono, fontWeight: 700, lineHeight: 1.25 }}>
            <div style={{ fontSize: 72, color: colors.white }}>It's not you.</div>
            <div style={{ fontSize: 72, color: colors.green, marginTop: 8 }}>
              It's designed that way.
            </div>
          </div>
        </Beat>
      </AbsoluteFill>

      {/* Layer 2: reveal + CTA */}
      {frame >= 278 && <Reveal />}
    </AbsoluteFill>
  );
};
