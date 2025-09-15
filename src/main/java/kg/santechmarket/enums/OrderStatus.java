package kg.santechmarket.enums;

/**
 * Статусы заказов
 */
public enum OrderStatus {
    /**
     * Новый заказ, ожидает обработки
     */
    PENDING,

    /**
     * Заказ подтвержден менеджером
     */
    CONFIRMED,

    /**
     * Заказ в процессе сборки
     */
    PROCESSING,

    /**
     * Заказ готов к выдаче/доставке
     */
    READY,

    /**
     * Заказ завершен
     */
    COMPLETED,

    /**
     * Заказ отменен
     */
    CANCELLED
}