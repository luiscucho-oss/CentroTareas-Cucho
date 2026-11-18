# CentroTareas

## Observación del Paso 1

Al pulsar **Tarea larga (MAL)** la pantalla se congela unos 4 segundos: el botón **Tócame** no suma, el botón no muestra la animación de pulsado y nada responde. Pasado ese tiempo aparece directamente **Terminó**.

**¿Por qué no se ve "Trabajando..."?** `Thread.sleep(4000)` corre en el hilo principal, que es el mismo que dibuja la pantalla. El mensaje cambia a "Trabajando...", pero el hilo queda dormido antes de poder dibujar el siguiente fotograma. Cuando despierta, el mensaje ya vale "Terminó", y eso es lo único que llega a dibujarse.

**¿Y si la espera fuera de 10 segundos?** Android mostraría el diálogo **ANR** («La aplicación no responde») con las opciones de esperar o cerrar la app. Un toque que no se atiende en unos 5 segundos dispara ese diálogo.
