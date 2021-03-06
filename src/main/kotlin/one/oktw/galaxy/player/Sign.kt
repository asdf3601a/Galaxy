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

package one.oktw.galaxy.player

import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.text.LiteralText
import one.oktw.galaxy.event.annotation.EventListener
import one.oktw.galaxy.event.type.PlayerInteractBlockEvent
import one.oktw.galaxy.event.type.PlayerUpdateSignEvent

class Sign {
    companion object {
        private val regex = Regex("&(?=[a-f0-9k-or])")
    }

    @EventListener(sync = true)
    fun onPlayerInteractBlock(event: PlayerInteractBlockEvent) {
        val player = event.player

        if (player.isSneaking && player.mainHandStack.isEmpty && player.offHandStack.isEmpty) {
            val entity = player.serverWorld.getBlockEntity(event.packet.blockHitResult.blockPos) as? SignBlockEntity ?: return
            player.openEditSignScreen(entity)
        }
    }

    @EventListener(sync = true)
    fun onPlayerUpdateSign(event: PlayerUpdateSignEvent) {
        for (i in 0..3) {
            event.blockEntity.setTextOnRow(i, LiteralText(regex.replace(event.packet.text[i], "§")))
        }

        event.cancel = true
    }
}
