# 1C:SSL support for 1C:EDT

Расширение для 1C:EDT позволяющее разрабатывать конфигурации на основе библиотеки 1С:БСП (Библиотека Стандартных Подсистем, Standard Subsystems Library) с бОльшим комфортом.

## Основные возможности

### Помощник ввода в строковых литералах

Поддержка контент-ассиста в строковых литералах, гиперссылки для перехода или по F3:

* [`ОбщегоНазначения.ПодсистемаСуществует` (`Common.SubsystemExist`)](common-subsystem-exist.md)
* [`ОбщегоНазначения.ОбщийМодуль` (`Common.CommonModule`)](common-common-module.md)
* [`ОбщегоНазначения.ЗначениеРеквизитаОбъекта` (`Common.ObjectAttributeValue`)](common-object-attribute-value.md)
* [`ОбщегоНазначения.ЗначениеРеквизитаОбъектов` (`Common.ObjectsAttributeValue`)](common-objects-attribute-value.md)
* [`ОбщегоНазначенияКлиент.ОбщийМодуль` (`CommonClient.CommonModule`)](common-client-common-module.md)
* [`ОбщегоНазначенияКлиент.ПодсистемаСуществует` (`CommonClient.SubsystemExist`)](common-client-subsystem-exist.md)


### Типизация возвращаемых значений

Позволяет на лету вычислять функции общего модуля `ОбщегоНазначения` (`Common`), которые возвращают тип в зависимости от переданных параметров.

* [`ОбщийМодуль` (`CommonModule`)](common-common-module.md)
* [`МенеджерОбъектаПоСсылке` (`ObjectManagerByRef`)](common-object-manager-by-ref.md)
* [`МенеджерОбъектаПоПолномуИмени` (`ObjectManagerByFullName`)](common-object-manager-by-full-name.md)
* [`ЗначениеРеквизитаОбъекта` (`ObjectAttributeValue`)](common-object-attribute-value.md)
* [`ЗначениеРеквизитаОбъектов` (`ObjectsAttributeValue`)](common-objects-attribute-value.md)
* [`ЗначенияРеквизитовОбъекта` (`ObjectAttributesValues`)](common-object-attributes-values.md)
* [`ЗначенияРеквизитовОбъектов` (`ObjectsAttributesValues`)](common-objects-attribute-value.md)
* [`ОписаниеСвойствОбъекта` (`ObjectPropertiesDetails`)](common-object-properties-details.md)
* [`ТаблицаЗначенийВМассив` (`ValueTableToArray`)](common-value-table-to-array.md)
* [`СтрокаТаблицыЗначенийВСтруктуру` (`ValueTableRowToStructure`)](common-value-table-row-to-structure.md)
* [`ФиксированныеДанные` (`FixedData`)](common-value-fixed-data.md)
* [`ВыгрузитьКолонку` (`UnloadColumn`)](common-unload-column.md)
* [`ПроверитьПроведенностьДокументов` (`CheckDocumentsPosting`)](common-check-documents-posting.md)
* [`СкопироватьРекурсивно` (`CopyRecursive`)](common-copy-recursive.md)

Позволяет на лету вычислять функции общего модуля `ОбщегоНазначенияКлииентСервер` (`CommonClientServer`)

* [`РазностьМассивов` (`ArraysDifference`)](common-client-server-arrays-difference.md)
* [`ЗначениеВМассиве` (`ValueInArray`)](common-client-server-value-in-array.md)
* [`СвернутьМассив` (`CollapseArray`)](common-client-server-collapse-array.md)

Позволяет на лету вычислять функции общего модуля `ОбщегоНазначенияКлииент` (`CommonClient`)

* [`ОбщийМодуль` (`CommonModule`)](common-client-common-module.md)
