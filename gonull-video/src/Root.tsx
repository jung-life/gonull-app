import React from "react";
import { Composition } from "remotion";
import { GoNullOnboarding } from "./GoNullOnboarding";

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
    </>
  );
};
