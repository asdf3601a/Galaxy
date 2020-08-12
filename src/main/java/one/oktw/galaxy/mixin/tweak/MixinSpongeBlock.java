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

package one.oktw.galaxy.mixin.tweak;

import net.minecraft.block.SpongeBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import one.oktw.galaxy.Main;
import one.oktw.galaxy.event.type.SpongeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpongeBlock.class)
public class MixinSpongeBlock {
    @Inject(method = "update", at = @At(value = "HEAD"))
    private void SpongeEvent(World world, BlockPos pos, CallbackInfo ci) {
        Main main = Main.Companion.getMain();
        if (main == null) return;
        main.getEventManager().emit(new SpongeEvent(world, pos));
    }
}