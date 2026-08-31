import React from "react";
import { Composition } from "remotion";
import { GoNullOnboarding } from "./GoNullOnboarding";
import { DoomscrollAd } from "./ads/DoomscrollAd";
import { Slide1, Slide2, Slide3, Slide4, Slide5 } from "./slides/Slides";

const IG_SLIDES = [Slide1, Slide2, Slide3, Slide4, Slide5];

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="GoNullOnboarding"
        component={GoNullOnboarding}
        durationInFrames={870}
        fps={30}
        width={1080}
        height={1920}
      />
      {/* Marketing: "The Scroll" — vertical doomscroll-interrupt ad (13s, real feed + music) */}
      <Composition
        id="DoomscrollAd"
        component={DoomscrollAd}
        durationInFrames={390}
        fps={30}
        width={1080}
        height={1920}
        defaultProps={{ feedBlur: 0 }}
      />
      {/* Publish-safe variant: same ad with the real feed blurred so faces/logos
          /content aren't identifiable (avoids right-of-publicity/copyright risk). */}
      <Composition
        id="DoomscrollAdBlurred"
        component={DoomscrollAd}
        durationInFrames={390}
        fps={30}
        width={1080}
        height={1920}
        defaultProps={{ feedBlur: 30 }}
      />

      {/* Instagram carousel slides — 1080x1350 (4:5), rendered as stills */}
      {IG_SLIDES.map((Comp, i) => (
        <Composition
          key={i}
          id={`Slide${i + 1}`}
          component={Comp}
          durationInFrames={1}
          fps={30}
          width={1080}
          height={1350}
        />
      ))}
    </>
  );
};
