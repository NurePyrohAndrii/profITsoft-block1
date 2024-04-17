## Як запустити програму

  Запустити проект можна наступним чином:
  - `клонуванти проект на свою машину`
  - `відкрити проект з папки json-parser, вони є кореневою папкою проєкта`
  - `запустити метод main з параметрами "src/main/resources/data {attribute}", де замість attribute можуть бути використані усі атрибути сутності Flight, яка представлена нижче`
## Опис основних сутностей

1. **Flight (Авіарейс)**: Представляє сутність польоту з такими атрибутами:
   - `flightNumber` (номер рейсу)
   - `departure` (місце відправлення)
   - `destination` (місце призначення)
   - `departureTime` (час відправлення)
   - `arrivalTime` (час прибуття)
   - `services` (послуги, доступні на рейсі)
  
    Варто зауважити, що departure та destination представленні у вигляді кодів IATA.

    Пул значень services представленні константним списком із значенями:
    - "Business Class",
    - "Economy Class",
    - "Premium Economy",
    - "First Class",
    - "Wi-Fi",
    - "Entertainment",
    - "Meals",
    - "Extra Legroom",
    - "Priority Boarding",
    - "In-flight Shopping"
  
  2. **Airport (Аеропорт)**: Ця сутність в коді наразі не представлена, але передбачається, що один аеропорт може обслуговувати багато рейсів.
  
  ## Приклади вхідних і вихідних файлів

  Передбачається формат вхідних файлів у вигляді JSON масиву об'єктів,<br>
  приклад такого файлу можете переглянути за посиланням - <a>https://github.com/NurePyrohAndrii/profITsoft-block1/blob/dev/profITsoft-block1/json-parser/src/main/resources/data/flights0.json</a>

  Вихідні ж файли представлені у XML форматі <br>
  Приклад такого файлу можете переглянути за посиланням - <a>https://github.com/NurePyrohAndrii/profITsoft-block1/blob/dev/profITsoft-block1/json-parser/src/main/resources/statistics_by_services.xml</a>
 
  ## Результати експериментів з різною кількістю потоків

  Для тестування з різною кількістю потоків було створено тестові дані. Вони собой являють 16 файлів по 100_000 JSON об'єктів. Тестові дані були згенеровані з допомогою бібліотеки javafaker
  <a>https://mvnrepository.com/artifact/com.github.javafaker/javafaker</a>, сам генератор представлений у класі dev.profitsoft.generator.FlightJsonDataGenerator. Файли генеруються у папку src/main/resources/data.  
  За тестування цього типу відповідає клас dev.profitsoft.parser.ParsePerformanceTest, вимірюванне значення часу виконання роботи сформоване як середнє з 5 вимірів для зменшення похибки.
  
  Результати тестування:
  - Parsing duration `3237` milliseconds with thread count `1` 
  - Parsing duration `3184` milliseconds with thread count `1` 
  - Parsing duration `1751` milliseconds with thread count `2` 
  - Parsing duration `1213` milliseconds with thread count `4` 
  - Parsing duration `811` milliseconds with thread count `8`

  Зі збільшенням кількості потоків додатковий приріст продуктивності зменшується, що є звичайним явищем у багатопотокових застосуваннях через накладні витрати на управління потоками та вплив операційної системи.
    
