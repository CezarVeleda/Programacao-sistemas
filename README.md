# PROJETO DE PROGRAMAÇÃO DE SISTEMAS

O trabalho consiste em implementar:
- Um simulador para um computador hipotético SIC
- Um montador de duas passagens
- Um processador de macros
- Um ligador implementado em duas passagens

A entrega do trabalho será feita por etapas (checkpoints)

## Etapas do Trabalho

### ETAPA 1: MAQUINA VIRTUAL
A etapa a seguir descrita consiste em implementar um simulador para um Computador Hipotético (SIC/XE), conforme apresentado no livro Sytems Software: An Introduction to System Programming de Leland L. Beck, com alterações e complementos de algumas funções. Tal sistema será composto de dois módulos que deverão operar de forma integrada: o executor (simulador propriamente dito) e uma interface visual.

CHECKPOINT:  29/09

 

O TRABALHO SERÁ UMA APRESENTAÇÃO EM VÍDEO DO GRUPO COM  TODOS PARTICIPANTES COM CÓDIGO DISPONIBILIZADO VIA GITHUB;

O ENVIO É FEITO APENAS POR COMPONENTE DO GRUPO;

A DURAÇÃO MÁXIMA DO VÍDEO DEVERÁ SER DE 10 MIN COM TOLERÂNCIA DE 5 MIN. (15) ;

A APRESENTAÇÃO DEVERÁ MOSTRAR:

1) INTERAÇÃO ENTRE OS COMPONENTES;

2) ARGUIÇÃO DO FUNCIONAMENTO E DAS TÉCNICAS USADAS.

A APRESENTAÇÃO NÃO DEVERÁ SER APENAS:

1) APRESENTAÇÃO DE SLIDES;  

2) APRESENTAÇÕES INDIVIDUAIS DOS COMPONENTES DO GRUPO.

### ETAPA 2: MONTADOR
Implementar um montador de duas passagens de acordo com as especificações.
 

CHECKPOINT:  13/10/2026


############################################################################################

O TRABALHO SERÁ UMA APRESENTAÇÃO EM VÍDEO DO GRUPO COM  TODOS PARTICIPANTES COM CÓDIGO DISPONIBILIZADO VIA GITHUB;

O ENVIO É FEITO APENAS POR COMPONENTE DO GRUPO;

A DURAÇÃO MÁXIMA DO VÍDEO DEVERÁ SER DE 10 MIN COM TOLERÂNCIA DE 5 MIN. (15) ;

A APRESENTAÇÃO DEVERÁ MOSTRAR:

1) INTERAÇÃO ENTRE OS COMPONENTES;

2) ARGUIÇÃO DO FUNCIONAMENTO E DAS TÉCNICAS USADAS.

 

A APRESENTAÇÃO NÃO DEVERÁ SER APENAS:

1) APRESENTAÇÃO DE SLIDES;  

2) APRESENTAÇÕES INDIVIDUAIS DOS COMPONENTES DO GRUPO.

############################################################################################



### ETAPA 3:PROCESSADOR DE MACROS
O processamento de macros deve ser realizado antes da montagem, sendo ativado a partir do módulo principal integrador do macro-montador. Deve permitir a definição de macros dentro de macros (macros aninhadas), bem como a chamada de macros dentro de macros (chamadas aninhadas), sendo, portanto, implementado em uma só passagem. O programa receberá como entrada um arquivo fonte informado para montagem e gerará como saída outro arquivo fonte com o nome MASMAPRG.ASM.

Processador de Macro: Checkpoint: 30/10/2027

### ETAPA 4: LIGADOR
O Ligador deverá ser implementado em duas passagens. Além da ligação, também executará a relocação completa de endereços, quando exigido ligador-relocador, considerando-se que o endereço de carga pode ser conhecido previamente (dependência de um Carregador Absoluto). Quando for exigido apenas um ligador, a finalização da relocação será deixada para o momento da carga (Carregador Relocador)

Ligador: Checkpoint: 13/11

 

############################################################################################

O TRABALHO SERÁ UMA APRESENTAÇÃO EM VÍDEO DO GRUPO COM  TODOS PARTICIPANTES COM CÓDIGO DISPONIBILIZADO VIA GITHUB;

O ENVIO É FEITO APENAS POR COMPONENTE DO GRUPO;

A DURAÇÃO MÁXIMA DO VÍDEO DEVERÁ SER DE 10 MIN COM TOLERÂNCIA DE 5 MIN. (15) ;

A APRESENTAÇÃO DEVERÁ MOSTRAR:

1) INTERAÇÃO ENTRE OS COMPONENTES;

2) ARGUIÇÃO DO FUNCIONAMENTO E DAS TÉCNICAS USADAS.

 

A APRESENTAÇÃO NÃO DEVERÁ SER APENAS:

1) APRESENTAÇÃO DE SLIDES;  

2) APRESENTAÇÕES INDIVIDUAIS DOS COMPONENTES DO GRUPO.

############################################################################################

## DESENVOLVIMENTO

Aqui estão definidas a arquitura do projeto.


Cada terá separado um java package específico, que eu irei referir como modulo.
Eu (Andriei) vou colocar só as funções publicas, e modelos do projeto.

### Lista atual dos modulos, e suas responsabilidades:

- GUI:
Responsável pelo:
    - Interface;
    - Input e Output dos dados;
    - comunicação do user com os modulos;

- Main:
Responsável pelo:
    - Instanciação dos modulos;
    - Enviar para o GUI uma ref dos modulos;
    - Catch de exceções caso um dos modulos emita uma exceção

- VirtualMachine:
Responsável pelo:
    - Maquina virtual (DUH!!!);

### Lista de Classes (por modulos)

- GUI:
    - Window
        Criação dos panels e setup da interface
    - MainPanel
        Possui botões para interacão com os modulos e sua funcionalidade,
        Ex.: Carregar um arquivo do PC, Run da maquina, Assembler Code, et cetera.
    - outros panels (vou ver a interface depois)

- Main:
    - Main

- VirtualMachine
    - OpCode (Enum)

        Guarda o op code e mnemonico, de cada op; \
        Possui as funções getCode, getMnemonic \
        OBS: **VOU DEIXAR PROTÓTIPO DO ENUM FEITO**

    - OP (Abstrata)

        Classe somente com funções sem implementação, com sobrecarga de funções; \
        Ex.: Solve(int A); \
             Solve(int A, int B);

    - OPX (Herda de OP) ("X" é a op, ex.: ADD, SUB, BR, et cetera)

        Implementa as funções padrões da "OP",

    - Machine (Abstrata)

        Possui funções de get/set padrão de qualquer máquina;  \
        Ex.: SetMachineCode (recebe o codigo de maquina e o deixa pronto na maquina virtual), \
             GetPC (retorna a posição atual do program counter), \
             et cetera. \
        OBS: **AS FUNÇÕES NÃO TEM IMPLEMENTAÇÃO!**
    
    - SIC (Herda "Machine")
        Implementa as funções padrões da "Machine", \
        Tem as operações da máquina em um hashmap,
        
        ----------------------------------------------------------
        pseudo código para exemplificar o hashmap com as operações:

        - Cria um hashmap de OP

            HashMap<String, OP> opMap = new HashMap<>();

        - Coloca a OPX num hashmap, onde a chave é a op de código de maquina (STRING);

            opMap.put(OpCode.ADD.getCode(), new OPAdd()); \
            opMap.put(OpCode.SUB.getCode(), new OPSub()); \
            opMap.put(OpCode.MULT.getCode(), new OPMult()); \
            opMap.put(OpCode.DIV.getCode(), new OPDiv());

        - Pega o OP

            OP currentOp = opMap.get(OPSub);

        - Usa o OP

            int res = currentOp.solve(a, b);

        ----------------------------------------------------------
