package eu.gir.girsignals.tileentitys;

import eu.gir.girsignals.blocks.SignalBlock;
import eu.gir.girsignals.blocks.SignalBlock.SignalAngel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {

	private static int MAX_WIDTH = 20;
	
	@Override
	public void render(SignalTileEnity te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		if(!te.hasCustomName()) return;
		IBlockState state = te.getWorld().getBlockState(te.getPos());
 		SignalAngel face = state.getValue(SignalBlock.ANGEL);
 		float angel = face.getAngel();
 		String display = te.getDisplayName().getFormattedText();
 		FontRenderer font = getFontRenderer();
 		
 		GlStateManager.pushMatrix();
 		GlStateManager.translate(x, y + te.getCustomNameRenderHeight(), z - MAX_WIDTH*0.015f/2);
 		GlStateManager.scale(0.015f, -0.015f, 0.015f);
 		GlStateManager.rotate(angel, 0, 1, 0);
 		GlStateManager.translate(face.getX() / 0.015f, 0, face.getY() / 0.015f);
 		font.drawSplitString(display, 0, 0, MAX_WIDTH, 0);
 		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean isGlobalRenderer(SignalTileEnity te) {
		return te.hasCustomName();
	}
}
