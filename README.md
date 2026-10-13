
# Android Tic-Tac-Toe

## Descripción



El proyecto consiste en un juego de Tic-Tac-Toe para Android en el que el usuario juega contra Android.

La aplicación permite:

- Jugar partidas de Tic-Tac-Toe contra Android.
- Detectar victorias, derrotas y empates.
- Iniciar una nueva partida desde el menú.
- Alternar quién comienza cada partida.
- Llevar un registro de victorias del usuario, victorias de Android y empates.

## Tecnologías

- Java
- XML
- Android Studio
- Git y GitHub

## Estructura principal

- `TicTacToeGame.java`: contiene la lógica del juego.
- `AndroidTicTacToeActivity.java`: controla la interfaz y la interacción con el usuario.
- `main.xml`: define la interfaz principal del juego.
- `menu.xml`: define las opciones del menú.
- `strings.xml`: contiene los textos utilizados por la aplicación.

## Funcionalidades

### Juego

El usuario juega utilizando los nueve botones que representan el tablero. Después de cada movimiento del usuario, Android realiza su movimiento automáticamente.

### Niveles de dificultad

El juego presenta tres niveles segú tu destreza:

**Easy:** Jugadas aleatorias.\
**Hard:** Primero intenta ganar; si no puede, juega aleatoriamente.\
**Expert:** Primero intenta ganar, después bloquear al jugador y, si ninguna de esas opciones existe, juega aleatoriamente. 

### Estadísticas

La aplicación mantiene el número de:

- Victorias del usuario.
- Victorias de Android.
- Empates.

### Inicio de las partidas

El jugador que comienza se alterna entre partidas nuevas.

# Changelog

## Reto 7 — Multijugador online y navegación entre modos

* Se integró Firebase Realtime Database para implementar partidas multijugador online.
* Se implementó la autenticación anónima de jugadores mediante Firebase Authentication.
* Se creó un lobby para crear partidas y unirse a partidas disponibles.
* Se implementó la sincronización del tablero, los turnos y los resultados entre dos jugadores.
* Se implementó la validación de movimientos para evitar jugar fuera de turno o en casillas ocupadas.
* Se creó una pantalla principal para seleccionar entre Un solo jugador y Modo online.
* Se adaptó el menú de opciones para mostrar las funciones correspondientes a cada modo de juego.
* Se agregó la opción Menú principal para regresar a la pantalla inicial desde las partidas individuales, las partidas online y el lobby.
* Se adaptó la opción New Game para iniciar una nueva partida individual o regresar al lobby en el modo online.
* Se recuperaron los efectos de sonido para los movimientos de ambos jugadores en las partidas online.
* Se implementaron contadores independientes de victorias, derrotas y empates para el modo online.
* Se agregó la persistencia local de las estadísticas del modo online mediante SharedPreferences.
* Se adaptó la opción Reset Scores para reiniciar las estadísticas del modo de juego actual.
* Se corrigieron errores de navegación entre partidas online y el lobby.
* Se verificó el funcionamiento independiente de los modos individual y multijugador.

## Reto 6 — Cambio de orientación y conservación del estado

* Se agregó soporte para orientación vertical y horizontal.
* Se creó el diseño `layout-land` para la orientación horizontal.
* Se implementó la conservación del tablero al cambiar la orientación.
* Se conserva el estado de la partida al recrearse la Activity.
* Se conserva el turno actual al cambiar la orientación.
* Se implementó el almacenamiento persistente de los marcadores mediante `SharedPreferences`.
* Los marcadores se conservan incluso después de salir y volver a abrir la aplicación.
* Se agregó la opción **Reset Scores** para reiniciar los marcadores.
* Se eliminó la opción **Quit** del menú.
* Se implementó la persistencia del nivel de dificultad seleccionado.
* Se verificó que la rotación durante el turno de Android no interrumpa la partida.

## Reto 5 — Gráficos y sonido

* Se reemplazó el tablero basado en botones por un `BoardView` personalizado.
* Se implementó el dibujo del tablero mediante `Canvas`.
* Se agregaron imágenes para representar las X y O.
* Se implementó la interacción táctil con el tablero.
* Se agregaron efectos de sonido para los movimientos del jugador y de Android.
* Se agregó un retraso de un segundo antes del movimiento de Android.
* Se implementó el control del turno mediante `mHumanTurn`.
* Se adaptó la lógica existente del juego al nuevo tablero gráfico.

## Reto 4 — Menús y cuadros de diálogo

* Se agregó un menú de opciones.
* Se agregó la opción **New Game**.
* Se agregaron los niveles de dificultad **Easy**, **Harder** y **Expert**.
* Se implementó una estrategia diferente para Android según el nivel de dificultad.
* Se agregó la lógica para detectar movimientos ganadores.
* Se agregó la lógica para bloquear movimientos del jugador.
* Se agregó la confirmación antes de salir de la aplicación.
* Se agregó un cuadro de diálogo **About** como desafío adicional.
* Se crearon los recursos necesarios para los menús y cuadros de diálogo.

## Reto 3 — Juego básico de Tic-Tac-Toe

* Se creó la aplicación Android Tic-Tac-Toe.
* Se implementó la lógica básica del juego.
* Se implementaron los jugadores humano y Android.
* Se implementó la detección de victorias y empates.
* Se agregó el conteo de victorias del jugador, victorias de Android y empates.
* Se agregó la opción **New Game**.
* Se implementó la alternancia del jugador que comienza cada partida.
* Se creó la interfaz básica mediante layouts XML.
* Se conectó la lógica del juego con la Activity de Android.
