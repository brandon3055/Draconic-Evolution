package com.brandon3055.draconicevolution.blocks.energynet;

import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.draconicevolution.api.IHudDisplay;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalDirectIO;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalRelay;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalWirelessIO;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 19/11/2016.
 */
public class EnergyCrystal extends BlockBCore implements IHudDisplay {
    private final TechLevel techLevel;
    private final CrystalType crystalType;
    private static VoxelShape CRYSTAL_SHAPE = VoxelShapes.create(new AxisAlignedBB(0.375, 0.125, 0.375, 0.625, 0.875, 0.625));
    private static VoxelShape[] IO_CRYSTAL_SHAPES = new VoxelShape[6];

    static {
        for (Direction dir : Direction.values()) {
            Cuboid6 c = new Cuboid6(0.35, 0, 0.35, 0.65, 0.425, 0.65);
            c.apply(Rotation.sideRotations[dir.getIndex()].at(Vector3.CENTER));
            IO_CRYSTAL_SHAPES[dir.getIndex()] = VoxelShapes.create(c.aabb());
        }
    }

    public EnergyCrystal(Properties properties, TechLevel techLevel, CrystalType crystalType) {
        super(properties);
        this.techLevel = techLevel;
        this.crystalType = crystalType;
    }

    //region Block

    @Override
    public boolean isBlockFullCube() {
        return false;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (crystalType == CrystalType.CRYSTAL_IO) {
            TileEntity tile = world.getTileEntity(pos);
            Direction facing = tile instanceof TileCrystalDirectIO ? ((TileCrystalDirectIO) tile).facing.get() : Direction.DOWN;
            return IO_CRYSTAL_SHAPES[facing.getIndex()];
        }
        return CRYSTAL_SHAPE; //Crystal
    }

    //    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void registerRenderer(Feature feature) {
//        ClientRegistry.bindTileEntitySpecialRenderer(TileCrystalRelay.class, new RenderTileEnergyCrystal());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileCrystalDirectIO.class, new RenderTileEnergyCrystal());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileCrystalWirelessIO.class, new RenderTileEnergyCrystal());
//        ModelRegistryHelper.registerItemRenderer(Item.getItemFromBlock(this), new RenderItemEnergyCrystal());
//        ModelRegistryHelper.register(new ModelResourceLocation(feature.getRegistryName(), "normal"), GlassParticleDummyModel.INSTANCE);
//        StateMap deviceStateMap = new StateMap.Builder().ignore(TIER).ignore(TYPE).build();
//        ModelLoader.setCustomStateMapper(this, deviceStateMap);
//    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

//    @Override
//    public BlockRenderLayer getRenderLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return crystalType.createTile(techLevel);
    }

    //endregion

    //region Info

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addDisplayData(@Nullable ItemStack stack, World world, @Nullable BlockPos pos, List<String> displayList) {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileCrystalBase)) {
            return;
        }

        displayList.add(InfoHelper.HITC() + asItem().getName().getString());
        TileCrystalBase tile = (TileCrystalBase) te;
        tile.addDisplayData(displayList);
    }

    //endregion

    public enum CrystalType implements IStringSerializable {
        RELAY(0) {
            @Override
            public TileEntity createTile(TechLevel techLevel) {
                return new TileCrystalRelay(techLevel);
            }
        }, CRYSTAL_IO(1) {
            @Override
            public TileEntity createTile(TechLevel techLevel) {
                return new TileCrystalDirectIO(techLevel);
            }
        }, WIRELESS(2) {
            @Override
            public TileEntity createTile(TechLevel techLevel) {
                return new TileCrystalWirelessIO(techLevel);
            }
        };

        private final int index;

        CrystalType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        private static CrystalType fromIndex(int i) {
            return i == 0 ? RELAY : i == 1 ? CRYSTAL_IO : WIRELESS;
        }

        public static CrystalType fromMeta(int meta) {
            return fromIndex(meta / 3);
        }

        public int getMeta(int tier) {
            return (getIndex() * 3) + tier;
        }

        public static int getTier(int meta) {
            return meta % 3;
        }

        @Override
        public String getString() {
            return name().toLowerCase();
        }

        public abstract TileEntity createTile(TechLevel techLevel);
    }
}
