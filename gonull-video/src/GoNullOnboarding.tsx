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

  const opacity = interpolate(frame, [0, 20], [0, 1], {
    extrapolateRight: "clamp",
  });

  const textOpacity = interpolate(frame, [18, 48], [0, 1], {
    extrapolateRight: "clamp",
  });

  // Second line fades in after the headline so they appear one after another.
  const subtitleOpacity = interpolate(frame, [56, 86], [0, 1], {
    extrapolateRight: "clamp",
  });

  const hourScale = spring({
    frame: frame - 92,
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

  const textOpacity = interpolate(frame, [28, 64], [0, 1], {
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

// The Science: why willpower isn't enough, and how friction rewires the loop.
const NeuroScience: React.FC<{ point: string; detail: string; delay: number; accent: string }> = ({
  point,
  detail,
  delay,
  accent,
}) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const slideIn = spring({ frame: frame - delay, fps, config: { damping: 16 } });
  const opacity = interpolate(frame, [delay, delay + 18], [0, 1], {
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        display: "flex",
        alignItems: "flex-start",
        gap: 28,
        opacity,
        transform: `translateY(${interpolate(slideIn, [0, 1], [40, 0])}px)`,
      }}
    >
      <div
        style={{
          minWidth: 14,
          height: 14,
          borderRadius: 7,
          marginTop: 16,
          backgroundColor: accent,
          boxShadow: `0 0 18px ${accent}`,
        }}
      />
      <div>
        <div
          style={{
            color: colors.white,
            fontSize: 42,
            fontWeight: "bold",
            fontFamily: "system-ui, sans-serif",
            lineHeight: 1.2,
          }}
        >
          {point}
        </div>
        <div
          style={{
            color: colors.gray,
            fontSize: 30,
            marginTop: 10,
            fontFamily: "system-ui, sans-serif",
            lineHeight: 1.3,
          }}
        >
          {detail}
        </div>
      </div>
    </div>
  );
};

const ScienceScene: React.FC = () => {
  const frame = useCurrentFrame();

  const titleOpacity = interpolate(frame, [0, 24], [0, 1], {
    extrapolateRight: "clamp",
  });

  // Closing synthesis line fades in after the three points have landed.
  const synthesisOpacity = interpolate(frame, [150, 178], [0, 1], {
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: colors.black,
        padding: 70,
        justifyContent: "center",
      }}
    >
      <h2
        style={{
          color: colors.green,
          fontSize: 52,
          fontWeight: "bold",
          marginBottom: 56,
          opacity: titleOpacity,
          fontFamily: "system-ui, sans-serif",
        }}
      >
        It's not you. It's the design.
      </h2>

      <div style={{ display: "flex", flexDirection: "column", gap: 40 }}>
        <NeuroScience
          point="Apps run on dopamine"
          detail="Endless feeds use unpredictable rewards — the same loop that drives slot machines."
          delay={30}
          accent={colors.red}
        />
        <NeuroScience
          point="Willpower loses in the moment"
          detail="Your impulsive brain reacts faster than the part that plans ahead."
          delay={70}
          accent={colors.yellow}
        />
        <NeuroScience
          point="GoNull adds a pause"
          detail="A few seconds of friction lets your thinking brain catch up — and, repeated, the craving fades."
          delay={110}
          accent={colors.green}
        />
      </div>

      <p
        style={{
          color: colors.white,
          fontSize: 38,
          fontWeight: "bold",
          marginTop: 60,
          textAlign: "center",
          opacity: synthesisOpacity,
          fontFamily: "system-ui, sans-serif",
        }}
      >
        We don't fight your willpower.
        <br />
        We help you rebuild the habit.
      </p>
    </AbsoluteFill>
  );
};

// Main Composition
export const GoNullOnboarding: React.FC = () => {
  return (
    <AbsoluteFill style={{ backgroundColor: colors.black }}>
      {/* Scene 1: The Problem (0-135 = 4.5s) — text lingers before the cut */}
      <Sequence from={0} durationInFrames={135}>
        <IntroScene />
      </Sequence>

      {/* Scene 2: Logo Reveal (135-260 = ~4s) */}
      <Sequence from={135} durationInFrames={125}>
        <LogoScene />
      </Sequence>

      {/* Scene 3: The Science (260-480 = ~7.3s) — the neuroscience level-set */}
      <Sequence from={260} durationInFrames={220}>
        <ScienceScene />
      </Sequence>

      {/* Scene 4: Progressive Friction Feature (480-640 = ~5.3s) */}
      <Sequence from={480} durationInFrames={160}>
        <FrictionFeatureScene />
      </Sequence>

      {/* Scene 5: Usage Mirror & Streaks (640-770 = ~4.3s) */}
      <Sequence from={640} durationInFrames={130}>
        <StatsFeatureScene />
      </Sequence>

      {/* Scene 6: Call to Action (770-870 = ~3.3s) */}
      <Sequence from={770} durationInFrames={100}>
        <CTAScene />
      </Sequence>
    </AbsoluteFill>
  );
};
