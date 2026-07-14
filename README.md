## Features

* Display a red moon during bloodmoons.
* Show the remaining time with a boss bar.
* Preserve active bloodmoons after server restarts.
* Synchronize the bloodmoon state with connected players.
* Control bloodmoons through commands (requieres admin perms).
* Use bloodmoon lifecycle events through the API.

### Start a bloodmoon

```mcfunction
/bloodmoon start <duration>
```

The duration is measured in ticks. There are 20 ticks in one second.

```mcfunction
/bloodmoon start 12000
```

### Stop the bloodmoon

```mcfunction
/bloodmoon stop
```

### Get the remaining time

```mcfunction
/bloodmoon get
```

### Set the remaining time

```mcfunction
/bloodmoon set <remainingTicks>
```

Setting the remaining time to `0` stops the bloodmoon.

### Change the remaining time

```mcfunction
/bloodmoon change <delta>
```

Positive values extend the bloodmoon. Negative values shorten it.

## API

### Control the bloodmoon

```java
Bloodmoon bloodmoon = Bloodmoon.getInstance();

bloodmoon.startBloodmoon(12_000);
bloodmoon.changeBloodmoonRemainingTicks(1_200);
bloodmoon.stopBloodmoon();
```

### Check the bloodmoon state

```java
if (Bloodmoon.isBloodmoonActive(level)) {
    // A bloodmoon is active.
}
```

### Register lifecycle events

```java
BloodmoonEvents.STARTED.register((bloodmoon, durationTicks) -> {
});

BloodmoonEvents.TICK.register((bloodmoon, remainingTicks) -> {
});

BloodmoonEvents.STOPPED.register(bloodmoon -> {
});
```

The tick event runs once per second while a bloodmoon is active.
