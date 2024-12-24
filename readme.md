<div align="center">
  <img src=".assets/illustration.png" width="500"/>
  <br>
  <img src="https://img.shields.io/badge/language-java-gold?style=flat" />
  <img src="https://img.shields.io/badge/beta-v1.0-gold?style=flat" />
  <img src="https://img.shields.io/github/stars/rmilansky/protocol?style=flat" />
</div>

# Введение

Minecraft Protocol - проект, написанный мной из-за того, что все библиотеки для взаимодействия с пакетами слишком устарели, либо недостаточно легки для использования в моих проектах (также из-за глобальной переписи всей кодовой базы [Abelix](https://abelix.team)).

Проект был написан буквально за один подход одним человеком, возможны баги/некрасивый код, но буду очень рад любым пулл реквестам и issue.

<img src=".assets/time.jpg" width="500"/>


# Краткий обзор проекта

## Для чего это всё?
Проект нужен для максимально удобной и интегрируемой разработки систем, для которых нужно использование пакетов протокола Minecraft (e.g. фейк энтити, неймтеги для серверов, прокси системы).

## Основные задачи

* Максимально простое взаимодействие с пакетами и их прослушкой
* Интеграция во все современные ядра / standalone приложения

# Руководство по использованию

Более глубокие примеры использования можно посмотреть в директории [examples](examples).
Но если попытаться объяснить кратко, то вот, например, как прослушать все ClientboundTeam пакеты: 

1. Создаем сам хандлер, который будет заниматься обработкой пакетов:
```java
@Log4j2
public final class ClientboundTeamHandler {
    @PacketProcessor
    public @NotNull PacketHandleResult handle(final ClientboundTeam team) {
        // Логируем, что сервер пытается отправить пакет
        log.info("Outbound team packet: {}", team);

        if (team.containsPlayer("milanskyy")) {
            // Предотвращаем отправку пакета
            return BasePacketHandleResult.cancel();
        }

        // Всё окей, просто разрешаем его отправку
        return BasePacketHandleResult.ok();
    }
}
```
2. Добавляем его игроку при входе:
```java
public final class NametagListener implements Listener {
    @EventHandler
    public void onCreate(final ProtocolPlayerCreateEvent event) {
        val protocolPlayer = event.protocolPlayer();

        // Так как это хандлер базированный на аннотациях, оборачиваем его в AnnotationBasedHandler
        // и непосредственно добавляем игроку
        protocolPlayer.appendPacketHandler(AnnotationBasedHandler.create(NametagDebugHandler.create()));
    }
}
```

# Credits
Большое спасибо за идеи и информацию этим проектам:
* [Velocity](https://github.com/PaperMC/Velocity), [BungeeCord](https://github.com/SpigotMC/BungeeCord) - за некоторые идеи и структуру пакетов
* [BridgeNet](https://github.com/MikhailSterkhov/bridgenet) - за идею этого прекрасного readme