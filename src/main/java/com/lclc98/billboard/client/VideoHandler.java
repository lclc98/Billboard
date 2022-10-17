package com.lclc98.billboard.client;

import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.client.video.VideoDisplay;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class VideoHandler {

    private static final Map<BlockPos, VideoDisplay> VIDEO_DISPLAY_MAP = new HashMap<>();

    public static void addVideo(BillboardTileEntity te) {
        if (!VIDEO_DISPLAY_MAP.containsKey(te.getBlockPos())) {
            VIDEO_DISPLAY_MAP.put(te.getBlockPos(), new VideoDisplay(te, 0.5f, false));
        }
    }

    public static VideoDisplay getVideo(BillboardTileEntity te) {
        if (te != null) {
            return getVideo(te.getBlockPos());
        }
        return null;
    }

    public static VideoDisplay getVideo(BlockPos pos) {
            return VIDEO_DISPLAY_MAP.get(pos);
    }

    public static void removeVideo(BillboardTileEntity te) {
        VIDEO_DISPLAY_MAP.remove(te.getBlockPos());
    }
}
