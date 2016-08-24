#!/usr/bin/env python
# -*- coding: utf-8 -*-

from docopt import docopt
import json

USO = """
Usage:
    generaMap.py [options] json <localizacion> <anchoImagen> <altoImagen> <anchoCelda> <altoCelda>
    generaMap.py map [options] <fichero_json>
    generaMap.py minimap <fichero_json>
    generaMap.py --help

Options:
    --help               Muestra esta pantalla

    json                                        Genera fichero json con butacas

    -x <xini>                                   Desplazamiento inicial X [default: 0]
    -y <yini>                                   Desplazamiento inicial Y [default: 0]
    -d --descendente                            Los números de butacas van en orden descendente
    -s --serpiente                              Los números de butacas van en orden de serpiente
    -c --continuadas                            Los números de butacas siguen un progreso continuo independientemente de la fila
    -D --filadescendente                        Los números de filas van en orden descendente
    -i <incremento> --incremento <incremento>   Los números de butaca van incrementandose en este numero [default: 1]
    -f <filaInicial>                            Número de fila inicial [default: 0]
    -b <butacaInicial>                          Número de butaca inicial (default: Calculada automáticamente)
    <anchoImagen>                               Ancho de imagen
    <altoImagen>                                Alto de imagen
    <anchoCelda>                                Ancho de celda
    <altoCelda>                                 Alto de celda

    map                                         Genera map para incluir en el HTML

    -I --incluye                                Incluye los números de butaca en el recuadro de la butaca
    -a --area                                   Genera html con areas de coordenadas

    minimap

"""

def genera_json(localizacion, x_ini, y_ini, ancho_imagen, alto_imagen, ancho_celda, alto_celda, descendente, inc_butaca, fila_ini, butaca_ini, descendente_fila, serpiente, continuadas):

    butacas = []

    if descendente_fila:
        fila = fila_ini 
    else:
        fila = (alto_imagen-y_ini) / alto_celda + (fila_ini-1)
    #print fila
    if descendente:
        inc_butaca = -inc_butaca

    if descendente_fila:
        inc_fila = 1
    else:
        inc_fila = -1

    if butaca_ini != None:
        numero = int(butaca_ini)
    else:    
        if descendente: 
            numero = ((ancho_imagen-x_ini) / ancho_celda) * abs(inc_butaca)
        else:    
            numero = 1
    max_butaca = numero

    for y in range(y_ini, alto_imagen, alto_celda):
        for x in range(x_ini, ancho_imagen, ancho_celda):
            butacas.append({"localizacion":localizacion, "xIni":x, "yIni":y, "xFin":x+ancho_celda, "yFin":y+alto_celda, "fila":fila, "numero":numero})
            max_butaca = max(numero, max_butaca)
            numero += inc_butaca

        if serpiente or continuadas:
            if not descendente:
                inc_butaca = -inc_butaca
                if inc_butaca > 0:
                    numero = max_butaca + inc_butaca
                else:
                    numero = max_butaca + ((ancho_imagen-x_ini) / ancho_celda) * abs(inc_butaca)
        else:
            if butaca_ini != None:
                numero = int(butaca_ini)
            else:    
                if descendente: 
                    numero = ((ancho_imagen-x_ini) / ancho_celda) * abs(inc_butaca)
                else:    
                    numero = 1
        fila += inc_fila

    return json.dumps(butacas, sort_keys=True, indent=4, separators=(',', ': '))


def genera_map(fichero, incluye, area):

    #st = '<map name="map">\n'

    st = ''

    if area:
        st += '<area shape="rect" coords="%d,%d,%d,%d" th:href="\'javascript:Butacas.selecciona(\\\'%s\\\', \\\'\' + #{butacasFragment.%s} + \'\\\', %d, %d, %d, %d)\'" title="Butaca %d fila %d"/>\n' % (butaca['xIni'], butaca['yIni'], butaca['xFin'], butaca['yFin'], butaca['localizacion'], butaca['localizacion'], butaca['fila'], butaca['numero'], butaca['xIni'], butaca['yIni'], butaca['numero'], butaca['fila'])
    else:
        xMin = float("inf");
        yMin = float("inf");
        xMax = 0;
        yMax = 0;
        for butaca in json.load(open(fichero)):
            if butaca['xIni'] < xMin: xMin = butaca['xIni']
            if butaca['yIni'] < yMin: yMin = butaca['yIni']
            if butaca['xIni'] > xMax: xMax = butaca['xIni']
            if butaca['yIni'] > yMax: yMax = butaca['yIni']

        filas = []
        columnas = []
        for butaca in json.load(open(fichero)):

            if butaca["fila"] not in filas:
                filas.append(butaca["fila"])
                st += '<div class="mapaTexto" style="text-align: center;line-height: 29px;position: absolute;top: %spx; left: %spx;">%s</div>'%(butaca['yIni'], xMin - 19, butaca['fila'])
                st += '<div class="mapaTexto" style="text-align: center;line-height: 29px;position: absolute;top: %spx; left: %spx;">%s</div>'%(butaca['yIni'], xMax + 19, butaca['fila'])

            if incluye:
                st += '<div class="mapaTexto" style="cursor: pointer;z-index: 1;text-align: center;line-height: 29px;position: absolute;top: %spx; left: %spx;" th:onClick="\'javascript:Butacas.selecciona(\\\'%s\\\',\\\'\' + #{butacasFragment.%s} + \'\\\', %s, %s, %s, %s)\'">%s</div>'%(butaca['yIni'], butaca['xIni'], butaca['localizacion'], butaca['localizacion'], butaca['fila'], butaca['numero'], butaca['xIni'], butaca['yIni'], butaca['numero'])
            else:
                if butaca["numero"] not in columnas:
                    columnas.append(butaca["numero"])
                    st += '<div class="mapaTexto" style="text-align: center;line-height: 29px;position: absolute;top: %spx; left: %spx;">%s</div>'%(yMin - 28, butaca['xIni'], butaca['numero'])
                    st += '<div class="mapaTexto" style="text-align: center;line-height: 29px;position: absolute;top: %spx; left: %spx;">%s</div>'%(yMax + 28, butaca['xIni'], butaca['numero'])

            style = 'mapa'
            if butaca['localizacion'].startswith('discap'):
                style = 'mapa discap'

            st += '<div th:class="\'%s \' + ${estilosOcupadas.%s_%s_%s != null ? estilosOcupadas.%s_%s_%s : \'mapaLibre\'}" style="position: absolute;left: %spx;top: %spx;" id="%s-%s-%s" th:onClick="\'javascript:Butacas.selecciona(\\\'%s\\\',\\\'\' + #{butacasFragment.%s} + \'\\\', %s, %s, %s, %s)\'"></div>\n'%(style, butaca['localizacion'], butaca['fila'], butaca['numero'], butaca['localizacion'], butaca['fila'], butaca['numero'], butaca['xIni'], butaca['yIni'], butaca['localizacion'], butaca['fila'], butaca['numero'], butaca['localizacion'], butaca['localizacion'], butaca['fila'], butaca['numero'], butaca['xIni'], butaca['yIni'])


    #st += '</map>\n'

    return st

def genera_minimap(fichero):

    st = ''

    for butaca in json.load(open(fichero)):
        butaca['xIni'] = butaca['xIni'] / 2
        butaca['yIni'] = butaca['yIni'] / 2

        style = 'minimapa'
        st += '<div id="%s-%s-%s-mini" th:class="\'%s \' + ${estilosOcupadas.%s_%s_%s != null ? estilosOcupadas.%s_%s_%s : \'mapaLibre\'}" style="position: absolute;left: %spx;top: %spx;"></div>\n'%(butaca['localizacion'], butaca['fila'], butaca['numero'], style, butaca['localizacion'],
                                                                                                                                                                                                       butaca['fila'], butaca['numero'], butaca['localizacion'],
                                                                                                                                                                                                       butaca['fila'],
                                                                                                                                                                                     butaca['numero'], butaca['xIni'], butaca['yIni'])


    return st

if __name__ == "__main__":
    arguments = docopt(USO)

    #print arguments

    if arguments['json']:

        print genera_json(arguments["<localizacion>"], int(arguments["-x"]), int(arguments["-y"]), int(arguments["<anchoImagen>"]), int(arguments["<altoImagen>"]), int(arguments["<anchoCelda>"]), int(arguments["<altoCelda>"]), arguments["--descendente"], int(arguments['--incremento']), int(arguments['-f']), arguments['-b'], arguments['--filadescendente'], arguments["--serpiente"], arguments["--continuadas"])
    
    elif arguments['map']:
        
        print genera_map(arguments['<fichero_json>'], arguments["--incluye"], arguments["--area"])

    else:

        print genera_minimap(arguments['<fichero_json>'])


