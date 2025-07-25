package net.sixik.researchtree.research;

public enum ResearchHideTypeRender {
    QUESTION_STYLE, //Рендерит знак вопросса
    BLACKOUT_STYLE, //Рендерит только очертание предмета/иконки
    HIDE_STYLE,     //Полностью скрыто. Не видно пока условия не выполнены,
    EMPTY           //У исследования видно иконку и текст, но оно будет слегка тусклое и не кликабельное
}
