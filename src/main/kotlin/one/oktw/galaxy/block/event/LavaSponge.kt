/*
 * OKTW Galaxy Project
 * Copyright (C) 2018-2020
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package one.oktw.galaxy.block.event

import com.google.common.collect.Lists
import net.minecraft.block.*
import net.minecraft.fluid.Fluids
import net.minecraft.tag.FluidTags
import net.minecraft.util.Pair
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import one.oktw.galaxy.event.annotation.EventListener
import one.oktw.galaxy.event.type.SpongeEvent
import java.util.*

class LavaSponge {
    @EventListener(sync = true)
    fun LavaSponge(event: SpongeEvent) {
        if (absorbLava(event.world, event.pos)) {
            event.world.setBlockState(event.pos, Blocks.FIRE.defaultState, 2)
            event.world.syncWorldEvent(2001, event.pos, Block.getRawIdFromState(Blocks.LAVA.getDefaultState()))
        }
    }
    fun absorbLava(world: World, pos: BlockPos): Boolean {
        val queue: Queue<Pair<BlockPos, Int>> = Lists.newLinkedList()
        queue.add(Pair<BlockPos, Int>(pos, 0))

        var i = 0

        while (!queue.isEmpty()) {
            val pair: Pair<BlockPos, Int> = queue.poll() as Pair<BlockPos, Int>
            val blockPos = pair.left as BlockPos
            val j = pair.right as Int
            val var8 = Direction.values()
            val var9 = var8.size
            for (var10 in 0 until var9) {
                val direction = var8[var10]
                val blockPos2 = blockPos.offset(direction)
                val blockState = world.getBlockState(blockPos2)
                val fluidState = world.getFluidState(blockPos2)
                val material = blockState.material
                if (fluidState.isIn(FluidTags.LAVA)) {
                    if (blockState.block is FluidDrainable && (blockState.block as FluidDrainable).tryDrainFluid(
                            world,
                            blockPos2,
                            blockState
                        ) !== Fluids.EMPTY
                    ) {
                        ++i
                        if (j < 6) {
                            queue.add(Pair<BlockPos, Int>(blockPos2, j + 1))
                        }
                    } else if (blockState.block is FluidBlock) {
                        world.setBlockState(blockPos2, Blocks.AIR.defaultState, 3)
                        ++i
                        if (j < 6) {
                            queue.add(Pair<BlockPos, Int>(blockPos2, j + 1))
                        }
                    } else if (material == Material.UNDERWATER_PLANT || material == Material.REPLACEABLE_UNDERWATER_PLANT) {
                        val blockEntity = if (blockState.block.hasBlockEntity()) world.getBlockEntity(blockPos2) else null
                        Block.dropStacks(blockState, world, blockPos2, blockEntity)
                        world.setBlockState(blockPos2, Blocks.AIR.defaultState, 3)
                        ++i
                        if (j < 6) {
                            queue.add(Pair<BlockPos, Int>(blockPos2, j + 1))
                        }
                    }
                }
            }
            if (i > 64) {
                break
            }
        }

        return i > 1
    }
}
