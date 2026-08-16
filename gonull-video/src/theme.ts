// Shared brand tokens for GoNull video/ad compositions.
// Colors match the Android theme (Color.kt) and website Tailwind tokens 1:1.
// Fonts match the website: Inter (body) + JetBrains Mono (headlines/labels).
import { loadFont as loadInter } from "@remotion/google-fonts/Inter";
import { loadFont as loadMono } from "@remotion/google-fonts/JetBrainsMono";

export const colors = {
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

const { fontFamily: interFamily } = loadInter("normal", {
  weights: ["400", "600", "700"],
  subsets: ["latin"],
});
const { fontFamily: monoFamily } = loadMono("normal", {
  weights: ["500", "700"],
  subsets: ["latin"],
});

export const fonts = {
  sans: interFamily, // Inter — body copy
  mono: monoFamily, // JetBrains Mono — headlines, labels, CTAs
};
