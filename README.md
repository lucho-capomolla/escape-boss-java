# Escape Boss — portado de Wollok a Java

Este proyecto es 1) una mini-librería (`wgame`) que imita la API de Wollok
Game usando Java + Swing, y 2) el puerto real de tu juego "Escape Boss"
(paquete `juego`), a partir de los .wlk que me pasaste.

## ✅ Puerto completo

Ya tengo los 9 archivos de tu juego (`configuraciones`, `direcciones`,
`jugador`, `objetivos`, `oficina`, `menuInicio`, `pantallaJuego`, `sonidos`,
`theGame`) más los 3 que faltaban (`jefe`, `niveles`, `tarjetas`). Todo el
paquete `juego` es un puerto fiel — ya no queda ningún stub inventado.

Dos comportamientos del Wollok original que preservé tal cual (no son
bugs que haya introducido yo, ya estaban en tu código):

- **`Tarjetas.PERDER_ENERGIA`**: `perderEnergia.wlk` define un método
  `porcentaje(...)` configurable, pero `producirEfecto()` usa el
  **literal `15`** en vez de la variable `porcentaje`. Lo dejé igual,
  con un comentario en el código.
- **`Tarjetas.JEFE1_A_OBJETO` / `JEFE2_A_OBJETO`** (no se usan en ningún
  mazo del juego, así que es código muerto igual que en el original):
  `jefe1AObjeto`/`jefe2AObjeto` llaman `game.hasVisual(objetoVigilado)`
  pasándole una **posición**, no un visual — no calza con la firma real
  de `hasVisual`. Lo traduje a la intención más probable ("¿hay algo en
  esa celda?").

## Estructura

```
src/
  wgame/              <- la "librería puente" (equivalente al motor de Wollok Game)
    Position.java, Visual.java, GameObject.java, Keyboard.java,
    Sound.java, StaticImage.java, WGame.java, GamePanel.java
  juego/              <- el puerto real de "Escape Boss"
    Configuraciones.java   <- configuraciones.wlk
    Direcciones.java       <- direcciones.wlk
    Personaje.java         <- class Personaje (de jugador.wlk)
    Jugador.java           <- jugador.wlk
    Objetivos.java         <- objetivos.wlk (Tarea/Pedido/ObjetoEstado)
    Oficina.java            <- oficina.wlk (Consumible/Mochila/Planta/etc.)
    MenuInicio.java, Dificultades.java, Instrucciones.java  <- menuInicio.wlk
    PantallaJuego.java      <- pantallaJuego.wlk
    Sonidos.java            <- sonidos.wlk
    Estado.java, EstadoMenuInicio.java, EstadoDificultad.java, EstadoJuego.java
    Dificultad.java
    TheGame.java            <- theGame.wpgm (clase con el main)
    Jefe.java               <- jefe.wlk
    Niveles.java            <- niveles.wlk
    Tarjetas.java           <- tarjetas.wlk
  demo/
    PepitaDemo.java     <- ejemplo simple de referencia (no es parte del juego real)
pom.xml                 <- build Maven -> jar ejecutable (mainClass: juego.TheGame)
resources/              <- poné acá tus assets, con la MISMA estructura de
                           carpetas que usás en Wollok: Fondos/, Oficina/,
                           Objetivos/, Personaje/, Sonidos/, BarraEnergia/
```

## Tabla de equivalencias Wollok -> Java

| Wollok                                   | Java (esta librería)                              |
|-------------------------------------------|----------------------------------------------------|
| `object pepita { ... }`                   | `class Pepita { ... }` + una única instancia (o `enum` de un solo valor si querés forzar singleton) |
| `game.at(x, y)`                           | `WGame.get().at(x, y)`                              |
| `game.addVisual(obj)`                     | `WGame.get().addVisual(obj)`                        |
| `game.removeVisual(obj)`                  | `WGame.get().removeVisual(obj)`                     |
| `game.title("...")`, `width()`, `height()`, `cellSize()` | métodos equivalentes en `WGame.get()` |
| `game.start()`                            | `WGame.get().start()`                               |
| `game.onTick(ms, "nombre", { ... })`      | `WGame.get().onTick(ms, "nombre", () -> { ... })`   |
| `game.say(visual, "mensaje")`             | `WGame.get().say(visual, "mensaje")`                |
| `method position() = posicion`            | `public Position position() { return posicion; }`  |
| `method image() = "x.png"`                | `public String image() { return "x.png"; }`         |
| `method onCollideWith(otra) { ... }`      | `public void onCollideWith(Visual otra) { ... }`    |
| `keyboard.right().onPressDo({ ... })`     | `Keyboard.get().right().onPressDo(() -> { ... })`   |
| Closures `{ ... }`                        | Lambdas `() -> { ... }`                             |
| Mixins                                    | Interfaces con métodos `default`, o composición     |
| Tipado dinámico                           | Hay que declarar tipos explícitos                   |

## Cómo portar tu juego real (paso a paso)

1. **Cada `object` u `class` de Wollok** que tenga `position()` e `image()`
   se convierte en una clase Java que extiende `GameObject` (o implementa
   `Visual` directamente si necesitás más control).
2. **Los métodos** se copian casi literal, cambiando la sintaxis
   (`var` -> tipo explícito, `+=` sigue igual, bloques `{ }` -> lambdas
   cuando son parámetros de otro método).
3. **`program` / `main` de Wollok** se convierte en el `main(String[] args)`
   de tu clase de arranque (ver `TheGame.main`, ya wireado).
4. **Las imágenes** que uses en Wollok (`recursos/imagen.png`) se copian a
   `resources/images/` con el mismo nombre de archivo.
5. Compilá y probá con Maven (ver abajo) antes de portar todo el juego de
   una vez — conviene migrar un objeto a la vez y probar.

## Compilar y ejecutar (necesitás JDK 17+ instalado)

```bash
mvn clean package
java -jar target/mi-juego.jar
```

El `pom.xml` ya apunta a `juego.TheGame` como clase principal — no hace
falta tocar nada para compilar y correr el juego real.

## Exportar como aplicación de escritorio (.exe / .app / binario)

### Opción rápida: jar portable (necesita tener Java instalado)
`mvn clean package` ya te deja `target/mi-juego.jar`, corrible en
cualquier SO con `java -jar mi-juego.jar` — sirve para probar rápido o
compartir con alguien que ya tenga Java, pero no es lo que buscás si
querés algo tipo "hacer doble clic y listo, sin instalar nada".

### Opción real: ejecutable nativo con jpackage (sin pedir Java instalado)

### ⚠️ Importante: `jpackage` genera el instalador para el sistema operativo en el que lo corrés

No hay forma de generar un `.exe` de Windows compilando desde Mac o
Linux (ni al revés) — Java no cross-compila instaladores nativos. Si
querés un `.exe`, estos pasos los tenés que correr *en una máquina
Windows* (real o una VM). Si estás en Mac o Linux, te va a generar el
equivalente de tu sistema (`.app` o binario Linux).

### Requisitos (una sola vez)
- **JDK 17+** instalado (trae `jpackage` incluido, no hace falta instalar nada aparte)
- **Maven** instalado (`mvn` en el PATH)
- Tus assets (imágenes, sonidos) puestos en `resources/` en la raíz del
  proyecto, respetando las subcarpetas que usa el código
  (`Fondos/`, `Oficina/`, `Objetivos/`, `Personaje/`, `Sonidos/`,
  `BarraEnergia/`, `Tarjetas/`, `Jefe/`) — Maven ya está configurado
  para empaquetarlos dentro del jar automáticamente.

### Windows
```bat
scripts\build-exe.bat
```
Te deja el ejecutable portable en `dist\MiJuego\MiJuego.exe` — comprimís
esa carpeta entera y la compartís tal cual, no hace falta tener Java
instalado del otro lado para correrla.

### Mac / Linux
```bash
chmod +x scripts/build-app.sh
./scripts/build-app.sh
```
Genera el equivalente (`.app` en Mac, carpeta ejecutable en Linux) en `dist/`.

### ¿Instalador de verdad, con Setup.exe y desinstalador?
Los scripts de arriba usan `--type app-image` (carpeta portable, sin
instalador). Si en cambio querés un instalador con ícono en el menú
inicio y desinstalador:
- **Windows**: instalá [WiX Toolset v3](https://wixtoolset.org/releases/)
  y agregalo al PATH, después corré el mismo comando de `jpackage` que
  hay comentado al final de `build-exe.bat`, cambiando `--type app-image`
  por `--type exe` (o `msi`).
- **Mac**: `--type dmg` o `--type pkg` (no necesita nada extra).
- **Linux**: `--type deb` o `--type rpm` según tu distro.

Para agregarle un ícono propio, sumá `--icon ruta/a/icono.ico` (Windows),
`.icns` (Mac) o `.png` (Linux) a cualquiera de los comandos.

## Logging y trazabilidad

Todo el motor (`wgame`) y el juego (`juego`) loguean a través de
`wgame.Log` — sin dependencias externas, con nivel, timestamp y
categoría. Por defecto se ve por consola en nivel `INFO` (arranque,
cambios de estado/pantalla, carga de niveles, energía, game over,
dificultad del jefe, etc.).

```
java -jar target/mi-juego.jar                    # nivel INFO (por defecto)
java -Dlog.level=DEBUG -jar target/mi-juego.jar   # + cada tecla, movimiento, colisión, sonido/imagen cargados
```

Además, `TheGame.main()` llama a `Log.toFile("game.log")` al arrancar,
así que además de la consola queda un archivo `game.log` en el
directorio desde el que corriste el jar (se va agregando entre
corridas, no se pisa) — útil para mandarme el log si algo se rompe.

## Pantalla responsive, pantalla completa y grilla

- **Responsive de verdad**: `cellSize` sigue siendo siempre el valor lógico
  que configuraste (60 en Escape Boss) — nada del código del juego cambia.
  Lo que cambia es que `GamePanel` escala el dibujado completo para que
  el tablero entre siempre entero en la ventana, sin deformarse (se
  recalcula en cada frame, así que si redimensionás la ventana se ajusta
  solo, en vivo).
- **Pantalla completa**: apretá **F11** en cualquier momento para alternar
  entre ventana y pantalla completa real (modo exclusivo del sistema
  operativo). Si preferís que arranque directo en pantalla completa,
  `game.fullscreen(true)` antes de `game.start()` (ya está la línea
  comentada en `TheGame.java`).
- **Grilla**: ahora está **oculta por defecto**. Si te sirve verla para
  debuguear un nivel (ver bien en qué celda cae cada objeto), activala con
  `WGame.get().showGrid(true)`.

## Globitos de diálogo

`game.say(visual, mensaje)` ahora dibuja un globito de historieta de
verdad (burbuja redondeada + colita apuntando al objeto, con salto de
línea automático si el texto es largo) en vez de solo texto suelto.
Además, el globito **desaparece solo a los 2.5 segundos** — antes se
quedaba pegado hasta el próximo `say()` sobre el mismo objeto.

## Convertir los .mp3 a .wav

`javax.sound.sampled` (el reproductor de audio que usa `wgame.Sound`) no
soporta mp3 de forma nativa, solo wav/au/aiff. Incluí un script listo
para usar:

```bash
chmod +x scripts/convertir-mp3-a-wav.sh
./scripts/convertir-mp3-a-wav.sh
```

Requiere `ffmpeg` instalado (`brew install ffmpeg` / `apt install ffmpeg`
/ [ffmpeg.org](https://ffmpeg.org/download.html) en Windows). Convierte
todo lo que haya en `resources/Sonidos/*.mp3` a `.wav` con el formato
exacto que Java reproduce sin problemas (PCM 16-bit, 44100 Hz), al lado
del mp3 original (no lo borra). Después solo hay que cambiar la
extensión en el código donde se pida ese sonido, por ejemplo:


Si no querés instalar ffmpeg, alternativas con interfaz gráfica:
[Audacity](https://www.audacityteam.org/) (abrís el mp3, "Exportar como
WAV") o convertidores online como CloudConvert.

## Pausa

Apretá **P** en cualquier momento para pausar/reanudar (tecla configurable
con `WGame.get().pauseKey(KeyEvent.VK_ESCAPE)` antes de `start()`, por si
preferís otra). Mientras está en pausa:
- Se muestra una leyenda "PAUSA — Presioná P para continuar" superpuesta
  sobre todo el tablero.
- Se frenan todos los `onTick` (el jefe deja de perseguir, no aparecen
  tarjetas nuevas).
- Las teclas del juego (flechas, E, Enter, 1/2/3) se ignoran — la única
  que sigue funcionando es la de pausa, para reanudar.

Es una tecla de la ventana en sí (como F11 para pantalla completa),
separada del `keyboard` que ve Wollok, así que no hace falta tocar nada
de `configuraciones.wlk`.

## Volver al menú de inicio al terminar el juego

Antes, al perder (energía en 0, atrapado por el jefe) o ganar, el juego
esperaba unos segundos mostrando la pantalla final y directamente
**cerraba la aplicación**. Ahora, en los 3 casos, después de esos
segundos se llama a `PantallaJuego.volverAlMenu()`, que:
1. Reinicia todo el estado de la partida (energía del jugador, tareas,
   pedidos, objetos personales, compañeris ayudados, mochila, nivel
   actual) — para que una partida nueva no arranque con cosas ya
   completadas de la anterior.
2. Vacía el tablero y vuelve a mostrar la pantalla de inicio
   (`MenuInicio.iniciar()`), lista para jugar de nuevo sin haber cerrado
   la ventana en ningún momento.

Cerrar la aplicación de verdad sigue siendo simplemente cerrar la
ventana (la ✕), como cualquier programa de escritorio.

## Límites del tablero

`GameObject` ahora no permite que un objeto se posicione fuera de la
grilla: `WGame.clampToBoard(Position)` recorta la posición al borde más
cercano, y `moveTo()`/`right()`/`left()`/`up()`/`down()` pasan siempre por
ahí. Esto aplica tanto al moverse como a la posición inicial pasada al
constructor.

Por defecto el objeto queda "pegado" al borde (clamp). Si en cambio
preferís que el movimiento se **ignore por completo** cuando se saldría
del tablero (el objeto se queda exactamente donde estaba, no en el
borde), reemplazá en `GameObject`:

```java
public void moveTo(Position p) {
    if (WGame.get().isInsideBoard(p)) {
        this.position = p;
    }
    // si no entra, no hace nada: el objeto no se mueve
}
```

Si un `Visual` no extiende `GameObject` (porque implementa `Visual`
directamente), hay que agregar la misma validación a mano en su propio
`moveTo`/setter de posición, usando `WGame.get().isInsideBoard(p)` o
`WGame.get().clampToBoard(p)`.

## Limitaciones a tener en cuenta

- El "tablero" de Wollok Game se sirve en el navegador; acá se dibuja en
  una ventana Swing nativa (más apropiado para exportar como app).
- Las colisiones se detectan por igualdad de posición (misma celda), igual
  que el comportamiento típico de Wollok Game con objetos de una celda.
- Si tu juego original usa métodos específicos de `game.wlk` que no están
  acá (sonidos, `game.ground()`, animaciones por frames, etc.), hay que
  agregarlos a `WGame`/`GamePanel` siguiendo el mismo patrón.