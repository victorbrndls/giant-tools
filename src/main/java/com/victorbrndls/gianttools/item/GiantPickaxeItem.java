package com.victorbrndls.gianttools.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.w3c.dom.Text;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiantPickaxeItem extends PickaxeItem {

    private final Tiers tier;

    public GiantPickaxeItem(Tiers tier) {
        super(tier, 1, -2.8f, new Properties().tab(CreativeModeTab.TAB_TOOLS));
        this.tier = tier;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState blockState, BlockPos blockPos,
                             LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player) {
            Player player = (Player) entity;
            var blocksToBreak = getBlocksToBreak(level, blockPos, player);

            blocksToBreak.forEach((pos -> {
                var blockToBreakState = level.getBlockState(pos);
                var blockToBreak = blockToBreakState.getBlock();

                blockToBreak.playerDestroy(level, player, pos, blockToBreakState, null, stack);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            }));

            stack.hurtAndBreak(blocksToBreak.size(), entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return false;
    }

    private List<BlockPos> getBlocksToBreak(Level level, BlockPos centerBlock, Player player) {
        final var blockHitResult = player.level.clip(new ClipContext(getStartVector(player), getEndVector(player),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        final var blockHitDirection = blockHitResult.getDirection();
        final var playerFacingDirection = getPlayerFacingDirection(player);

        var blocks = switch (tier) {
            case STONE -> Arrays.asList(centerBlock, centerBlock.below());
            case IRON, GOLD -> getBlocksToBreak(centerBlock, blockHitDirection, 1, 1, playerFacingDirection);
            case DIAMOND -> getBlocksToBreak(centerBlock, blockHitDirection, 2, 1, playerFacingDirection);
            case NETHERITE -> getBlocksToBreak(centerBlock, blockHitDirection, 3, 1, playerFacingDirection);
            default -> List.of(centerBlock);
        };

        return blocks.stream().filter((blockPos) -> level.getBlockState(blockPos).getDestroySpeed(level, blockPos) != 0.0F).toList();
    }

    private @Nonnull List<BlockPos> getBlocksToBreak(BlockPos centerBlock, Direction hitDirection, int horizontal,
                                                     int vertical, Direction playerDirection) {
        var blocks = new ArrayList<BlockPos>((horizontal * 2 + 1) * (vertical * 2 + 1));
        var isXAxisDirection = playerDirection == Direction.WEST || playerDirection == Direction.EAST;

        for (int h = -horizontal; h <= horizontal; h++) {
            for (int v = -vertical; v <= vertical; v++) {
                var x = switch (hitDirection) {
                    case DOWN, UP -> isXAxisDirection ? v : h;
                    case NORTH, SOUTH -> h;
                    case WEST, EAST -> 0;
                };
                var y = switch (hitDirection) {
                    case NORTH, SOUTH, WEST, EAST -> v;
                    case DOWN, UP -> 0;
                };
                var z = switch (hitDirection) {
                    case WEST, EAST -> h;
                    case DOWN, UP -> !isXAxisDirection ? v : h;
                    case NORTH, SOUTH -> 0;
                };

                blocks.add(centerBlock.offset(x, y, z));
            }
        }

        return blocks;
    }

    private Vec3 getStartVector(Player player) {
        return new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
    }

    private Vec3 getEndVector(Player player) {
        Vec3 headVec = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vec3 lookVec = player.getViewVector(1.0F);
        double reach = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        return headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
    }

    private Direction getPlayerFacingDirection(Player player) {
        Vec3 lookVec = player.getViewVector(1.0F);

        if (lookVec.x < 0) {
            if (lookVec.z < 0) {
                return Direction.NORTH;
            } else {
                return Direction.WEST;
            }
        } else {
            if (lookVec.z < 0) {
                return Direction.EAST;
            } else {
                return Direction.SOUTH;
            }
        }
    }

}