
Процедура Tест()

    Соответствие = Новый Соответствие();
    Соответствие.Вставить("Ключ1", Новый Структура("Ключ1", 10));
    Соответствие.Вставить("Ключ2", "Значение1");
    
    ФиксированноеСоотвествие = ОбщегоНазначения.ФиксированныеДанные(Соответствие);
    // Часть кода для документации
    Если ФиксированноеСоотвествие.Ключ1.Ключ1 = 10 ИЛИ ФиксированноеСоотвествие.Ключ2 = "Значение1" Тогда
         // Продолжаем...
    КонецЕсли

КонецПроцедуры

