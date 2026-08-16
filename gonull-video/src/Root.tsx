import React from "react";
import { Composition } from "remotion";
import { GoNullOnboarding } from "./GoNullOnboarding";
import { DoomscrollAd } from "./ads/DoomscrollAd";

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
      {/* Marketing: "The Scroll" — vertical doomscroll-interrupt ad (12s, real feed + music) */}
      <Composition
        id="DoomscrollAd"
        component={DoomscrollAd}
        durationInFrames={360}
        fps={30}
        width={1080}
        height={1920}
      />
    </>
  );
};
