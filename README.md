# EnxServices

Projeto criado para etapa pratica na vaga Desenvolvedor Junior na EnxServices.

## Como rodar?

### Docker

A forma mais facil seria por meio do Docker!<br>
-> Lembrando que sera necessario adicionar valores validos para o banco de dados em `/plugins/Homes/database.json`

```shell
docker run --rm -p 25575:25575 -e MC_PORT=25575 -e EULA=true shiftsad/enxservices
```

### Plugin

Caso mais conveniente, tambem e possivel baixar e rodar o plugin diretamente no servidor.<br>
-> Lembrando que sera necessario adicionar valores validos para o banco de dados em `/plugins/Homes/database.json`<br>
[WindCharge](https://file.garden/ZoTRYFZJg1bmA4WJ/WindCharge.jar),
[Homes](https://file.garden/ZoTRYFZJg1bmA4WJ/Homes.jar),
[CommandAPI](https://file.garden/ZoTRYFZJg1bmA4WJ/CommandAPI-9.5.1.jar) (Dependencia)

# Homes

### Requisitos:

- [x] Cooldown configurável
    - Cooldown configuravel, tanto como tambem contagem regressiva,
      e outras variaveis como cancelar ao dano, ou movimento.
    - Alem de configuracoes basicas, a maioria das mensagens sao
      configuradas por meio do arquivo "messages.properties", que
      converte do formato MiniMessage, para um componente Json que
      o Minecraft entende, permitindo usar de todas as features do chat!
- [x] Configuração para exibir particulas ao teletransportar.
    - Nao simplesmente exibindo particulas, como uma engine simples
      e escalavel para exibir e configurar particulas. Capaz de renderizar
      qualquer imagem em tempo real (e fazer gira!), como efeitos genericos,
      tipo um círculo animado.
    - Como um pequeno extra, tambem e possivel configurar o som que
      sera tocado ao teletransportar.
- [x] Todas as homes devem ser salvas em um banco de dados.
    - Uma arquitetura abstraida de banco de dados, feita para permitir
      diversas data sources, porem optimizado para SQL. Por padrao, usa e
      abusa de piscinas, como "piscinas" (pools) de threads, e de conexoes SQL :)
    - Todos requests sao geridos de forma assincrona, por meio de features modernas
      do java, como CompletableFutures (ngc maneiro, recomendo).

# WindCharge

### Requisitos:

- [x] Força da explosão configurável
  - Forca da explosao realmente funcionando, essa parte deu muito mais trabalho que deveria,
  recomendo verificar nas outras submissões, pois acredito que a maioria dos candidatos
  nao fizeram exatamente como o planejado, dando umas trapaceadas, como, por exemplo, 
  aplicar o modificador a qualquer explosao!
- [x] Opção para adicionar partículas
    - Suporte a mesma engine de particulas do plugin Homes
- [x] Velocidade do projétil configurável

### Showcase

(Videos, clique para assistir)
[![Home Command](https://file.garden/ZoTRYFZJg1bmA4WJ/home-cmds.jpg)](https://file.garden/ZoTRYFZJg1bmA4WJ/home-cmds.mp4)
[![Debug Particles](https://file.garden/ZoTRYFZJg1bmA4WJ/admin-view.jpg)](https://file.garden/ZoTRYFZJg1bmA4WJ/admin-view.mp4)
[![Admin Views](https://file.garden/ZoTRYFZJg1bmA4WJ/debug-particle.jpg)](https://file.garden/ZoTRYFZJg1bmA4WJ/debug-particle.mp4)
