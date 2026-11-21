package net.smok.om.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.smok.om.Values;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpellTickManager extends SavedData {


    // 1. Кодек для самого менеджера
    public static final Codec<SpellTickManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellRunner.CODEC.listOf().fieldOf("active_runners").forGetter(manager -> manager.activeRunners)
    ).apply(instance, SpellTickManager::new));

    // 2. РЕГИСТРАЦИОННЫЙ ТИП (SavedDataType) под Fabric 26.1+
    public static final SavedDataType<SpellTickManager> TYPE = new SavedDataType<>(
            Values.id("active_spells"), // Путь к файлу в world/data
            SpellTickManager::new, // Дефолтный конструктор, если файла еще нет на диске
            CODEC,                 // Наш кодек
            null                   // DataFixerTypes (не требуется для новых систем)
    );

    private final List<SpellRunner> activeRunners = new ArrayList<>();
    private boolean isInitialized = false;

    // Пустой конструктор для создания новых данных
    public SpellTickManager() {
    }

    // Конструктор, используемый кодеком при загрузке
    private SpellTickManager(List<SpellRunner> loadedRunners) {
        this.activeRunners.addAll(loadedRunners);
    }

    /**
     * Получение менеджера через DimensionDataStorage
     */
    public static SpellTickManager get(ServerLevel level) {
        // В Fabric 26.1 computeIfAbsent принимает только объект SavedDataType
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Главный метод тика (вызывается из эвента)
     */
    public void tick(ServerLevel level) {
        // Ленивая инициализация ранеров при загрузке мира
        if (!isInitialized) {
            for (SpellRunner runner : activeRunners) {
                runner.init(level);
            }
            isInitialized = true;
        }

        if (activeRunners.isEmpty()) return;

        boolean changed = false;
        for (int i = activeRunners.size() - 1; i >= 0; i--) {
            if (activeRunners.get(i).tick()) {
                activeRunners.remove(i);
                changed = true;
            }
        }

        // Если список изменился или внутри тикают Wait/индексы, помечаем для сохранения
        if (changed || !activeRunners.isEmpty()) {
            setDirty();
        }
    }

    /**
     * Запуск нового заклинания
     */
    public void startSpell(SpellRunner runner) {
        this.activeRunners.add(runner);
        setDirty();
    }

    public List<SpellRunner> getActiveRunners() {
        return activeRunners;
    }
}
