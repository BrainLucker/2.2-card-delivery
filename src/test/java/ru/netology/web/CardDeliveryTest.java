package ru.netology.web;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class CardDeliveryTest {

    String setDeliveryDate(long daysToAdd) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.now().plusDays(daysToAdd).format(formatter);
    }

    boolean isNextMonth(String deliveryDate, String defaultDate) {
        return !(deliveryDate.substring(4, 6).equals(defaultDate.substring(4, 6)));
    }

    @BeforeEach
    void setUp() {
        Configuration.holdBrowserOpen = true;
        Configuration.browserSize = "1000x800";
        open("http://localhost:9999");
    }

    @Test
    void shouldSubmitValidForm() {
        String deliveryDate = setDeliveryDate(5); // переносим доставку с сегодняшней даты на несколько дней (минимум 3)
        String notificationText = "Встреча успешно забронирована на " + deliveryDate;

        SelenideElement form = $(".form");
        form.$("[data-test-id=city] input").val("Нарьян-Мар");
        form.$("[data-test-id=date] input.input__control").doubleClick().sendKeys(deliveryDate);
        form.$("[data-test-id=name] input").val("Уильям Мак-Кинли");
        form.$("[data-test-id=phone] input").val("+79991234567");
        form.$("[data-test-id=agreement] .checkbox__box").click();
        form.$$("button").find(exactText("Забронировать")).click();

        $("[data-test-id=notification] .notification__content").should(appear, Duration.ofSeconds(15)).shouldHave(text(notificationText));
    }

    @Test
    void shouldSubmitValidFormComplexElements() {
        SelenideElement form = $(".form");
        SelenideElement calendar = $(".popup .calendar");

        String city = "Казань";
        String deliveryDate = setDeliveryDate(7); // задаем дату доставки через неделю
        Integer dayOfDelivery = Integer.parseInt(deliveryDate, 0, 2, 10); // получаем число месяца из даты доставки
        String defaultDate = form.$("[data-test-id=date] input.input__control").getAttribute("value"); // получаем из виджета дату доставки по-умолчанию
        String notificationText = "Встреча успешно забронирована на " + deliveryDate;
        form.$("[data-test-id=city] input").val(city.substring(0, 2)); // вводим первые две буквы города
        $$(".input__popup .menu-item__control").find(exactText(city)).click();
        form.$("[data-test-id=date] button").click();
        if (isNextMonth(deliveryDate, defaultDate)) { // проверяем нужно ли перелистнуть на следующий месяц в виджете календаря
            calendar.$("[data-step='1']").click();
        }
        calendar.$$(".calendar__day").find(exactText(dayOfDelivery.toString())).click();
        form.$("[data-test-id=name] input").val("Уильям Мак-Кинли");
        form.$("[data-test-id=phone] input").val("+79991234567");
        form.$("[data-test-id=agreement] .checkbox__box").click();
        form.$$("button").find(exactText("Забронировать")).click();

        $("[data-test-id=notification] .notification__content").should(appear, Duration.ofSeconds(15)).shouldHave(text(notificationText));
    }
}