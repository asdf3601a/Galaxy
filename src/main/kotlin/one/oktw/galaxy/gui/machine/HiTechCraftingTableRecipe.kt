package one.oktw.galaxy.gui.machine

import kotlinx.coroutines.experimental.launch
import one.oktw.galaxy.Main
import one.oktw.galaxy.data.DataUUID
import one.oktw.galaxy.galaxy.traveler.TravelerHelper
import one.oktw.galaxy.galaxy.traveler.data.Traveler
import one.oktw.galaxy.gui.GUI
import one.oktw.galaxy.gui.GUIHelper
import one.oktw.galaxy.gui.PageGUI
import one.oktw.galaxy.gui.view.GridGUIView
import one.oktw.galaxy.item.enums.ButtonType
import one.oktw.galaxy.item.type.Button
import one.oktw.galaxy.recipe.HiTechCraftingRecipe
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.InventoryArchetypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.property.InventoryTitle
import org.spongepowered.api.text.Text
import java.util.*
import java.util.Arrays.asList

class HiTechCraftingTableRecipe(private val player: Player, private val traveler: Traveler, private val recipe: HiTechCraftingRecipe) : GUI() {
    companion object {
        private val lang = Main.languageService.getDefaultLanguage()

        private enum class Action {
            NONE,
            CRAFT,
            CANCEL
        }

        private enum class Slot {
            NULL,
            INGREDIENT,
            CRAFT,
            RESULT,
            BORDER_TOP,
            BORDER_RIGHT_TOP,
            BORDER_RIGHT,
            BORDER_RIGHT_BOTTOM,
            BORDER_BOTTOM,
            BORDER_LEFT_BOTTOM,
            BORDER_LEFT,
            BORDER_LEFT_TOP,
            NUMBER,
            DUST
        }

        private val X = Slot.NULL
        private val A = Slot.BORDER_TOP
        private val B = Slot.BORDER_RIGHT_TOP
        private val C = Slot.BORDER_RIGHT
        private val D = Slot.BORDER_RIGHT_BOTTOM
        private val E = Slot.BORDER_BOTTOM
        private val F = Slot.BORDER_LEFT_BOTTOM
        private val G = Slot.BORDER_LEFT
        private val H = Slot.BORDER_LEFT_TOP
        private val M = Slot.DUST
        private val N = Slot.NUMBER
        private val O = Slot.INGREDIENT
        private val P = Slot.CRAFT
        private val Q = Slot.RESULT

        private const val WIDTH = 9
        private const val HEIGHT = 6

        private val layout: List<Slot> = asList(
            X, X, X, X, X, X, X, X, X,
            X, X, H, A, A, A, B, X, X,
            X, X, G, Q, X, P, C, X, X,
            X, X, F, E, E, E, D, X, X,
            M, N, N, N, N, N, N, N, N,
            O, O, O, O, O, O, O, O, O
        )

        private val numbers = asList(
            ButtonType.NUMBER_0, ButtonType.NUMBER_1, ButtonType.NUMBER_2, ButtonType.NUMBER_3, ButtonType.NUMBER_4,
            ButtonType.NUMBER_5, ButtonType.NUMBER_6, ButtonType.NUMBER_7, ButtonType.NUMBER_8, ButtonType.NUMBER_9
        ).map { Button(it).createItemStack().apply { offer(DataUUID(UUID.randomUUID())) } }
    }

    override val token: String = "HiTechCraftingTableRecipe-${UUID.randomUUID()}"
    override val inventory: Inventory = Inventory.builder()
        .of(InventoryArchetypes.DOUBLE_CHEST)
        .property(InventoryTitle.of(Text.of(lang["UI.Title.HiTechCraftingTableRecipe"])))
        .listener(InteractInventoryEvent::class.java, this::eventProcess)
        .build(Main.main)

    private val view: GridGUIView<Slot, Action> = GridGUIView(
        inventory,
        layout,
        Pair(WIDTH, HEIGHT)
    )

    init {
        offerPage(player, traveler)
        registerEvent(ClickInventoryEvent::class.java, this::clickEvent)
    }

    private fun offerPage(player: Player, traveler: Traveler) {
        offerDust()
        offerIngredient(player)
        offerResult()
        offerConfirm(player, traveler)
        offerBorder()
        offerEmpty()
    }

    private fun getGUIItem(type: ButtonType): ItemStack {
        return Button(type).createItemStack()
            .apply {
                offer(DataUUID(UUID.randomUUID()))
            }
    }

    private fun getNumbers(number: Int, length: Int): List<ItemStack> {
        val result = ArrayList<ItemStack>()
        var remain = number

        for (i in 0 until length) {
            val digit = remain % 10
            remain /= 10
            result += numbers[digit]
        }

        return result.reversed()
    }

    private fun offerDust() {
        view.setSlot(Slot.DUST, getGUIItem(ButtonType.STARS))

        getNumbers(recipe.getCost(), view.countSlots(Slot.NUMBER))
            .let {
                view.setSlots(Slot.NUMBER, it)
            }
    }

    private fun offerIngredient(player: Player) {
        recipe.previewRequirement(player).map {
            it.apply {
                offer(DataUUID(UUID.randomUUID()))
            }
        }.let {
            view.setSlots(Slot.INGREDIENT, it, null)
        }
    }

    private fun offerResult() {
        recipe.result().apply {
            offer(DataUUID(UUID.randomUUID()))
        }.let {
            view.setSlot(Slot.RESULT, it, null)
        }

    }

    private fun offerConfirm(player: Player, traveler: Traveler) {
        recipe.hasEnoughDust(traveler)
            .and(recipe.hasEnoughIngredient(player))
            .let {
                if (it) {
                    view.setSlot(Slot.CRAFT, getGUIItem(ButtonType.OK), Action.CRAFT)
                } else {
                    view.setSlot(Slot.CRAFT, getGUIItem(ButtonType.X), Action.CANCEL)
                }
            }
    }

    private fun offerBorder() {
        view.setSlot(Slot.BORDER_TOP, getGUIItem(ButtonType.GUI_TOP))
        view.setSlot(Slot.BORDER_RIGHT_TOP, getGUIItem(ButtonType.GUI_CORNER_TOP_RIGHT))
        view.setSlot(Slot.BORDER_RIGHT, getGUIItem(ButtonType.GUI_RIGHT))
        view.setSlot(Slot.BORDER_RIGHT_BOTTOM, getGUIItem(ButtonType.GUI_CORNER_BOTTOM_RIGHT))
        view.setSlot(Slot.BORDER_BOTTOM, getGUIItem(ButtonType.GUI_BOTTOM))
        view.setSlot(Slot.BORDER_LEFT_BOTTOM, getGUIItem(ButtonType.GUI_CORNER_BOTTOM_LEFT))
        view.setSlot(Slot.BORDER_LEFT, getGUIItem(ButtonType.GUI_LEFT))
        view.setSlot(Slot.BORDER_LEFT_TOP, getGUIItem(ButtonType.GUI_CORNER_TOP_LEFT))
    }

    private fun offerEmpty() {
        view.setSlot(Slot.NULL, getGUIItem(ButtonType.GUI_CENTER))
    }

    private fun clickEvent(event: ClickInventoryEvent) {
        event.isCancelled = true

        val action = view.getDataOf(event)?: return

        when (action) {
            Action.CANCEL -> {
                GUIHelper.close(token)
            }

            Action.CRAFT -> {
                launch {
                    val traveler = TravelerHelper.getTraveler(player).await()?: return@launch

                    if (recipe.hasEnoughIngredient(player) && recipe.hasEnoughDust(traveler)) {
                        if (recipe.consume(player, traveler)) {
                            val stack = recipe.result()
                            val item = player.world.createEntity(EntityTypes.ITEM,  player.position)

                            item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot())

                            player.world.spawnEntity(item)

                            offerPage(player, traveler)
                        }
                    }
                }
            }
        }
    }
}
