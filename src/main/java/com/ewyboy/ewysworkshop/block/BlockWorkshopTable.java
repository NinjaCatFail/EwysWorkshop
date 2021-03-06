package com.ewyboy.ewysworkshop.block;

import com.ewyboy.ewysworkshop.loaders.CreativeTabLoader;
import com.ewyboy.ewysworkshop.main.EwysWorkshop;
import com.ewyboy.ewysworkshop.tileentity.TileEntityTable;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWorkshopTable extends BaseBlock {

    public BlockWorkshopTable() {
        super(Material.rock);
        setCreativeTab(CreativeTabLoader.EwysWorkshopTab);
        setHardness(3.5F);
        setStepSound(soundTypePiston);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTable();
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] icons, front;

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[]  {
                register.registerIcon("production:bot"),
                register.registerIcon("production:top"),
                register.registerIcon("production:back"),
                register.registerIcon("production:front0"),
                register.registerIcon("production:left"),
                register.registerIcon("production:right"),
        };
        for (int i = 0; i < 8; i++) {
            front = new IIcon[]{
                    register.registerIcon("production:front" + i),
            };
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return getIconFromSideAndMeta(side, 2);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromSideAndMeta(int side, int meta) {
        return icons[getSideFromSideAndMeta(side, meta)];
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return getIconFromSideAndMeta(side, world.getBlockMetadata(x, y, z));
    }

    public static int getSideFromSideAndMeta(int side, int meta) {
        if (side <= 1) {
            return side;
        }else{
            int index = SIDES_INDICES[side - 2] - meta;
            if (index < 0) {
                index += SIDES.length;
            }
            return SIDES[index] + 2;
        }
    }

    public static int getSideFromSideAndMetaReversed(int side, int meta) {
        if (side <= 1) {
            return side;
        }else{
            int index = SIDES_INDICES[side - 2] + meta;
            index %= SIDES.length;

            return SIDES[index] + 2;
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            FMLNetworkHandler.openGui(player, EwysWorkshop.instance, 0, world, x, y, z);
        }
        return true;
    }

    private static final int[] SIDES_INDICES = {0, 2, 3, 1};
    private static final int[] SIDES = {0, 3, 1, 2};

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack item) {
        int rotation = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te instanceof IInventory) {
            IInventory inventory = (IInventory)te;
            for (int i = 0; i < inventory.getSizeInventory(); ++i) {
                ItemStack item = inventory.getStackInSlotOnClosing(i);

                if (item != null) {
                    float offsetX = world.rand.nextFloat() * 0.8F + 0.1F;
                    float offsetY = world.rand.nextFloat() * 0.8F + 0.1F;
                    float offsetZ = world.rand.nextFloat() * 0.8F + 0.1F;

                    EntityItem entityItem = new EntityItem(world, x + offsetX, y + offsetY, z + offsetZ, item.copy());
                    entityItem.motionX = world.rand.nextGaussian() * 0.05F;
                    entityItem.motionY = world.rand.nextGaussian() * 0.05F + 0.2F;
                    entityItem.motionZ = world.rand.nextGaussian() * 0.05F;

                    world.spawnEntityInWorld(entityItem);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
