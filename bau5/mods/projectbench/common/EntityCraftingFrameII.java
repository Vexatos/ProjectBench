package bau5.mods.projectbench.common;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import bau5.mods.projectbench.common.recipes.RecipeCrafter;
import bau5.mods.projectbench.common.recipes.RecipeManager;
import bau5.mods.projectbench.common.recipes.RecipeManager.RecipeItem;

/**
 * EntityCraftingFrameII
 *
 * @author _bau5
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */

public class EntityCraftingFrameII extends EntityCraftingFrame
{
	private RecipeItem lastRecipe = null;
	private ItemStack  toDisplay  = null;
	private boolean showing = false;
	
	private int timer = 0;
	
	private RecipeCrafter theCrafter = new RecipeCrafter();
	
	public EntityCraftingFrameII(World world, int x, int y, int z, int dir){
		super(world, x, y, z, dir);
	}
	
	public EntityCraftingFrameII(World world) {
		super(world);
	}
	
	@Override
	public boolean func_130002_c(EntityPlayer player) {
		if(player == null || !ProjectBench.MKII_ENABLED)
			return false;
		if(player.getHeldItem() != null/* && !player.getHeldItem().getItem().hasContainerItem()*/){
			ItemStack theStack = player.getHeldItem();
			if(theStack.isItemDamaged()){
				theStack = theStack.copy();
				theStack.setItemDamage(0);
			}
			if(lastRecipe == null || !OreDictionary.itemMatches(lastRecipe.result(), theStack, false)){
				lastRecipe = RecipeManager.instance().searchForRecipe(theStack, false);
			}
			if(lastRecipe == null)
				return false;
			ArrayList<ItemStack[]> recipeStacks = lastRecipe.alternatives(); 
			if(recipeStacks != null){
				ItemStack[] consolidatedInventory = theCrafter.consolidateItemStacks(player.inventory.mainInventory);
				for(ItemStack[] isa : recipeStacks){
					theCrafter.addInventoryReference(player.inventory.mainInventory);
					int numMade = theCrafter.consumeItems(isa, consolidatedInventory, lastRecipe.result(), player.isSneaking());
					if(numMade != 0){
						ItemStack toDispense = lastRecipe.result();
						updateDisplay(toDispense);
						toDispense.stackSize *= numMade;
						if(!worldObj.isRemote)
							super.dispenseItem(toDispense);
						theCrafter.onItemCrafted(toDispense, player.worldObj, player, numMade);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void func_110128_b(Entity par1Entity) {
        this.entityDropItem(new ItemStack(ProjectBench.instance.craftingFrameII), 0.0F);
	}
	
	private void updateDisplay(ItemStack theStack) {
		timer = 0;
		if(toDisplay == null || !OreDictionary.itemMatches(toDisplay, theStack, false)){
			toDisplay = theStack.copy();
			toDisplay.stackSize = 1;
		}
		if(toDisplay != null)
			setDisplayedItem(toDisplay);
		showing = true;
	}

	@Override
	public void onUpdate() {
		if(showing){
			timer++;
		}
		if(timer > 40){
			showing = false;
			reset();
		}
		super.onUpdate();
	}
}
