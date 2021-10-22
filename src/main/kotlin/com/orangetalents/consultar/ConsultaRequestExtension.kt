package com.orangetalents.consultar

import com.orangetalents.ConsultaReply
import com.orangetalents.ConsultaRequest
import com.orangetalents.ConsultaRequest.FiltroCase.*

fun ConsultaRequest.buscaDadosResponse(consultaService: ConsultaService): ConsultaReply{
    return when (filtroCase){
        PIXID -> consultaService.consultaPorPixId(pixId)
        CHAVE -> consultaService.consultaPorChave(chave)
        FILTRO_NOT_SET -> consultaService.consultaInvalida()
    }
}