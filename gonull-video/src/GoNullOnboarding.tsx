import React from "react";
import {
  AbsoluteFill,
  interpolate,
  useCurrentFrame,
  useVideoConfig,
  spring,
  Sequence,
  Easing,
} from "remotion";

// Color palette matching GoNull theme
const colors = {
  black: "#0A0A0A",
  surface: "#141414",
  border: "#262626",
  white: "#FAFAFA",
  gray: "#A3A3A3",
  grayDark: "#737373",
  green: "#22C55E",
  greenDark: "#16A34A",
  red: "#EF4444",
  yellow: "#EAB308",
};

// Intro Scene - The Problem
const IntroScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const opacity = interpolate(frame, [0, 15], [0, 1], {
    extrapolateRight: "clamp",
  });

  const textOpacity = interpolate(frame, [20, 36], [0, 1], {
    extrapolateRight: "clamp",
  });

  // Second line fades in after the headline so they appear one after another.
  const subtitleOpacity = interpolate(frame, [42, 58], [0, 1], {
    extrapolateRight: "clamp",
  });

  const hourScale = spring({
    frame: frame - 50,
    fps,
    config: { damping: 12 },
  });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        justifyContent: "center",
        alignItems: "center",
        padding: 60,
      }}
    >
      <div
        style={{
          opacity,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 180,
            marginBottom: 40,
          }}
        >
          📱
        </div>

        <h1
          style={{
            color: colors.white,
            fontSize: 72,
            fontWeight: "bold",
            marginBottom: 30,
            opacity: textOpacity,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          How many times did you lose focus today?
        </h1>

        <p
          style={{
            color: colors.gray,
            fontSize: 38,
            marginBottom: 30,
            opacity: subtitleOpacity,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Don't feel bad about it — the apps are
          <br />
          designed to make you do that.
        </p>

        <div
          style={{
            display: "flex",
            justifyContent: "center",
            gap: 40,
            marginTop: 60,
            transform: `scale(${hourScale})`,
          }}
        >
          <TimeBlock hours="2.5h" app="Instagram" color={colors.red} />
          <TimeBlock hours="1.8h" app="TikTok" color={colors.red} />
          <TimeBlock hours="1.2h" app="YouTube" color={colors.yellow} />
        </div>
      </div>
    </AbsoluteFill>
  );
};

const TimeBlock: React.FC<{ hours: string; app: string; color: string }> = ({
  hours,
  app,
  color,
}) => (
  <div
    style={{
      backgroundColor: colors.surface,
      padding: "30px 40px",
      borderRadius: 20,
      textAlign: "center",
    }}
  >
    <div
      style={{
        color,
        fontSize: 56,
        fontWeight: "bold",
        fontFamily: "system-ui, sans-serif",
      }}
    >
      {hours}
    </div>
    <div
      style={{
        color: colors.gray,
        fontSize: 28,
        marginTop: 10,
        fontFamily: "system-ui, sans-serif",
      }}
    >
      {app}
    </div>
  </div>
);

// Logo Reveal Scene
const LogoScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoScale = spring({
    frame,
    fps,
    config: { damping: 10, stiffness: 100 },
  });

  const textOpacity = interpolate(frame, [30, 50], [0, 1], {
    extrapolateRight: "clamp",
  });

  const glowIntensity = interpolate(
    frame,
    [0, 30, 60, 90],
    [0, 20, 10, 20],
    {
      extrapolateRight: "mirror",
    }
  );

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <div
        style={{
          transform: `scale(${logoScale})`,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 280,
            color: colors.green,
            fontWeight: "bold",
            fontFamily: "system-ui, sans-serif",
            textShadow: `0 0 ${glowIntensity}px ${colors.green}`,
          }}
        >
          Ø
        </div>

        <h1
          style={{
            color: colors.white,
            fontSize: 96,
            fontWeight: "bold",
            marginTop: 20,
            opacity: textOpacity,
            fontFamily: "system-ui, sans-serif",
            letterSpacing: 8,
          }}
        >
          GoNull
        </h1>

        <p
          style={{
            color: colors.gray,
            fontSize: 36,
            marginTop: 30,
            opacity: textOpacity,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Take back your time
        </p>
      </div>
    </AbsoluteFill>
  );
};

// Feature 1: Progressive Friction
const FrictionFeatureScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const titleOpacity = interpolate(frame, [0, 20], [0, 1], {
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        padding: 60,
      }}
    >
      <h2
        style={{
          color: colors.green,
          fontSize: 56,
          fontWeight: "bold",
          marginBottom: 60,
          opacity: titleOpacity,
          fontFamily: "system-ui, sans-serif",
        }}
      >
        Progressive Friction
      </h2>

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 30,
        }}
      >
        <FrictionLevel
          level={1}
          description="Quick verification"
          delay={20}
          color={colors.green}
        />
        <FrictionLevel
          level={2}
          description="5 minute wait"
          delay={35}
          color={colors.yellow}
        />
        <FrictionLevel
          level={3}
          description="15 min wait + reflection"
          delay={50}
          color={colors.red}
        />
        <FrictionLevel
          level={4}
          description="Locked until tomorrow"
          delay={65}
          color={colors.red}
        />
      </div>

      <p
        style={{
          color: colors.gray,
          fontSize: 32,
          marginTop: 60,
          textAlign: "center",
          opacity: interpolate(frame, [80, 100], [0, 1], {
            extrapolateRight: "clamp",
          }),
          fontFamily: "system-ui, sans-serif",
        }}
      >
        Each bypass gets harder.
        <br />
        Your future self will thank you.
      </p>
    </AbsoluteFill>
  );
};

const FrictionLevel: React.FC<{
  level: number;
  description: string;
  delay: number;
  color: string;
}> = ({ level, description, delay, color }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const slideIn = spring({
    frame: frame - delay,
    fps,
    config: { damping: 15 },
  });

  const opacity = interpolate(frame, [delay, delay + 15], [0, 1], {
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 30,
        transform: `translateX(${interpolate(slideIn, [0, 1], [-100, 0])}px)`,
        opacity,
      }}
    >
      <div
        style={{
          width: 80,
          height: 80,
          borderRadius: 40,
          backgroundColor: color,
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          fontSize: 36,
          fontWeight: "bold",
          color: colors.black,
          fontFamily: "system-ui, sans-serif",
        }}
      >
        {level}
      </div>
      <div
        style={{
          color: colors.white,
          fontSize: 40,
          fontFamily: "system-ui, sans-serif",
        }}
      >
        {description}
      </div>
    </div>
  );
};

// Feature 2: Usage Mirror & Streaks
const StatsFeatureScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const cardScale = spring({
    frame: frame - 20,
    fps,
    config: { damping: 12 },
  });

  const streakScale = spring({
    frame: frame - 50,
    fps,
    config: { damping: 10, stiffness: 80 },
  });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        justifyContent: "center",
        alignItems: "center",
        padding: 60,
      }}
    >
      <h2
        style={{
          color: colors.green,
          fontSize: 56,
          fontWeight: "bold",
          marginBottom: 60,
          fontFamily: "system-ui, sans-serif",
          textAlign: "center",
        }}
      >
        Know Your Usage
      </h2>

      {/* Usage Mirror Card */}
      <div
        style={{
          backgroundColor: colors.surface,
          borderRadius: 30,
          padding: 50,
          width: "90%",
          transform: `scale(${cardScale})`,
          marginBottom: 40,
        }}
      >
        <div
          style={{
            color: colors.gray,
            fontSize: 24,
            letterSpacing: 4,
            marginBottom: 30,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          YOUR TIME
        </div>

        <div
          style={{
            display: "flex",
            justifyContent: "space-around",
          }}
        >
          <StatBlock value="45m" label="Today" color={colors.green} />
          <div
            style={{
              width: 2,
              backgroundColor: colors.border,
            }}
          />
          <StatBlock value="3h 20m" label="This Week" color={colors.yellow} />
        </div>
      </div>

      {/* Streak Counter */}
      <div
        style={{
          backgroundColor: colors.surface,
          borderRadius: 30,
          padding: 40,
          width: "90%",
          transform: `scale(${streakScale})`,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 80,
          }}
        >
          🔥
        </div>
        <div
          style={{
            color: colors.white,
            fontSize: 72,
            fontWeight: "bold",
            fontFamily: "system-ui, sans-serif",
          }}
        >
          7 days
        </div>
        <div
          style={{
            color: colors.gray,
            fontSize: 28,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Don't break your streak!
        </div>
      </div>
    </AbsoluteFill>
  );
};

const StatBlock: React.FC<{ value: string; label: string; color: string }> = ({
  value,
  label,
  color,
}) => (
  <div style={{ textAlign: "center" }}>
    <div
      style={{
        color,
        fontSize: 56,
        fontWeight: "bold",
        fontFamily: "system-ui, sans-serif",
      }}
    >
      {value}
    </div>
    <div
      style={{
        color: colors.gray,
        fontSize: 28,
        fontFamily: "system-ui, sans-serif",
      }}
    >
      {label}
    </div>
  </div>
);

// Call to Action Scene
const CTAScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoScale = spring({
    frame,
    fps,
    config: { damping: 10 },
  });

  const buttonScale = spring({
    frame: frame - 40,
    fps,
    config: { damping: 12 },
  });

  const pulse = interpolate(
    frame,
    [60, 90, 120],
    [1, 1.05, 1],
    { extrapolateRight: "mirror" }
  );

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        justifyContent: "center",
        alignItems: "center",
        padding: 60,
      }}
    >
      <div
        style={{
          transform: `scale(${logoScale})`,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 200,
            color: colors.green,
            fontWeight: "bold",
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Ø
        </div>

        <h1
          style={{
            color: colors.white,
            fontSize: 64,
            fontWeight: "bold",
            marginTop: 40,
            marginBottom: 20,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Ready to focus?
        </h1>

        <p
          style={{
            color: colors.gray,
            fontSize: 36,
            marginBottom: 60,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Your time is valuable.
          <br />
          Protect it.
        </p>

        <div
          style={{
            backgroundColor: colors.green,
            color: colors.black,
            fontSize: 40,
            fontWeight: "bold",
            padding: "30px 80px",
            borderRadius: 20,
            transform: `scale(${buttonScale * pulse})`,
            fontFamily: "system-ui, sans-serif",
          }}
        >
          Get Started
        </div>
      </div>
    </AbsoluteFill>
  );
};

// Main Composition
export const GoNullOnboarding: React.FC = () => {
  return (
    <AbsoluteFill style={{ backgroundColor: colors.black }}>
      {/* Scene 1: The Problem (0-90 frames = 3 seconds) */}
      <Sequence from={0} durationInFrames={90}>
        <IntroScene />
      </Sequence>

      {/* Scene 2: Logo Reveal (90-180 frames = 3 seconds) */}
      <Sequence from={90} durationInFrames={90}>
        <LogoScene />
      </Sequence>

      {/* Scene 3: Progressive Friction Feature (180-300 frames = 4 seconds) */}
      <Sequence from={180} durationInFrames={120}>
        <FrictionFeatureScene />
      </Sequence>

      {/* Scene 4: Usage Mirror & Streaks (300-390 frames = 3 seconds) */}
      <Sequence from={300} durationInFrames={90}>
        <StatsFeatureScene />
      </Sequence>

      {/* Scene 5: Call to Action (390-450 frames = 2 seconds) */}
      <Sequence from={390} durationInFrames={60}>
        <CTAScene />
      </Sequence>
    </AbsoluteFill>
  );
};
