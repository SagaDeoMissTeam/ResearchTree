package net.sixik.researchtree.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;

public class UIColors {
    // Основной фон (чуть светлее, чтобы не сливаться с элементами)
    public static final RGBA BACKGROUND = RGBA.create(18, 18, 22, 255); // #121216, тёмно-серый

    // Основной фон модальной панели
    public static final RGBA MODAL_BACKGROUND = RGBA.create(44, 62, 80, 255); // #2C3E50, тёмно-серый с синеватым оттенком

    // Оверлей (затемнение фона)
    public static final RGBA MODAL_OVERLAY = RGBA.create(0, 0, 0, (int)(255 * 0.6)); // Чёрный с 60% прозрачностью

    // Основной цвет для виджетов (модальные панели, карточки)
    public static final RGBA WIDGET_BACKGROUND = RGBA.create(30, 30, 36, 255); // #1E1E24, тёмно-серый с лёгким синим оттенком

    // Основной акцентный цвет (кнопки, активные элементы)
    public static final RGBA PRIMARY = RGBA.create(59, 130, 246, 255); // #3B82F6, яркий синий

    // Вторичный акцентный цвет (для ховер-эффектов, ссылок)
    public static final RGBA SECONDARY = RGBA.create(147, 51, 234, 255); // #9333EA, фиолетовый

    // Цвет текста (основной, для хорошей читаемости)
    public static final RGBA TEXT_PRIMARY = RGBA.create(229, 231, 235, 255); // #E5E7EB, светло-серый

    // Цвет текста (вторичный, для подсказок или неактивных элементов)
    public static final RGBA TEXT_SECONDARY = RGBA.create(156, 163, 175, 255); // #9CA3AF, серый

    // Цвета состояния
    public static final RGBA SUCCESS = RGBA.create(34, 197, 94, 255); // #22C55E, зелёный
    public static final RGBA WARNING = RGBA.create(234, 179, 8, 255); // #EAB308, жёлтый
    public static final Color4I WARNING_FTB = Color4I.rgb(234, 179, 8); // #EAB308, жёлтый
    public static final RGBA ERROR = RGBA.create(239, 68, 68, 255); // #EF4444, красный

    // Границы (для разделителей или обводок)
    public static final RGBA BORDER = RGBA.create(55, 55, 61, 255); // #37373D, тёмно-серый
}
