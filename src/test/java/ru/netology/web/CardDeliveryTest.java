package ru.netology.web;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class CardDeliveryTest {

    String setDeliveryDate(int fewDays) {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        calendar.add(Calendar.DAY_OF_MONTH, fewDays);
        return formatter.format(calendar.getTime());
    }

    boolean isNextMonth(Integer dayOfDelivery) {
        Calendar calendar = new GregorianCalendar();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        return (dayOfDelivery < today);
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
        String city = "Казань";
        String deliveryDate = setDeliveryDate(7); // задаем дату доставки через неделю
        Integer dayOfDelivery = Integer.parseInt(deliveryDate.substring(0, 2)); // получаем число месяца из даты доставки
        String notificationText = "Встреча успешно забронирована на " + deliveryDate;

        SelenideElement form = $(".form");
        SelenideElement calendar = $(".popup .calendar");
        form.$("[data-test-id=city] input").val(city.substring(0, 2)); // вводим первые две буквы города
        $$(".input__popup .menu-item__control").find(exactText(city)).click();
        form.$("[data-test-id=date] button").click();
        if (isNextMonth(dayOfDelivery)) { // проверяем нужно ли перелистнуть на следующий месяц в виджете календаря
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