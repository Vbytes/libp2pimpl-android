package com.vbyte.p2p;

import android.os.Handler;

public class P2PHandler extends Handler {	
	public static final int p2p_ChannelInfoSuccess      = 0x00;
	public static final int p2p_FirstDataSuccess        = 0x01;
	public static final int p2p_SecondDataSuccess       = 0x02;
	public static final int p2p_FourthDataSuccess       = 0x03;
	
	public static final int p2p_WriteDataBlock          = 0x10;
	public static final int p2p_WriteDataUnblock        = 0x11;
	
	public static final int p2p_ChannelInfoFail         = 0x20;
	public static final int cdn_DownLoadFail            = 0x30;
}
