package com.zonainmueble.reports.utils;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.zonainmueble.reports.config.AppConfig;
import com.zonainmueble.reports.openai.CompletionRequest;
import com.zonainmueble.reports.openai.Message;
import com.zonainmueble.reports.openai.MessageRole;
import com.zonainmueble.reports.openai.OpenAIService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ConclusionUtils {
  private final AppConfig config;
  private final OpenAIService completionService;

  public String integralNivelBienestarIngresos(Map<String, Object> params) {
    String context = """
        Dentro del polígono de 5 minutos caminando existe una población total de [nse_iso5_poblacion] habitantes, el [nse_iso5_top_1_porcentaje]% tiene un nivel socio-económico
        [nse_top_1_nse] que es un nivel [nse_top_1_nombre] con ingresos mensuales promedio de [nse_top_1_desc]. El [nse_iso5_top_2_porcentaje]% tiene un nivel socio-económico
        [nse_top_2_nse] que es un nivel [nse_top_2_nombre] con ingresos mensuales promedio de [nse_top_2_desc], y el [nse_iso5_top_3_porcentaje]% tiene un nivel socio-económico
        [nse_top_3_nse] que es un nivel [nse_top_3_nombre] con ingresos mensuales promedio de [nse_top_3_desc].
        Para el polígono de 10 minutos caminando existe una población total de [nse_iso10_poblacion] habitantes, el [nse_iso10_top_1_porcentaje]% tiene un nivel socio-económico
        [nse_top_1_nse]. El [nse_iso10_top_2_porcentaje]% tiene un nivel socio-económico [nse_top_2_nse] y el [nse_iso10_top_3_porcentaje]% tiene un nivel socio-económico [nse_top_3_nse].
        Para el polígono de 15 minutos caminando existe una población total de [nse_iso15_poblacion] habitantes, el [nse_iso15_top_1_porcentaje]% tiene un nivel socio-económico
        [nse_top_1_nse]. El [nse_iso15_top_2_porcentaje]% tiene un nivel socio-económico [nse_top_2_nse] y el [nse_iso15_top_3_porcentaje]% tiene un nivel socio-económico [nse_top_3_nse].
        En general el ingreso mensual promedio de esta zona de análisis es de [nse_ingreso_promedio] pesos MXN. 
        NOTA: Has una conclusión con esta información comenzando con la frase "Revela una notable" o "Denota...". Puedes generar máximo 2 parrafos con por lo menos un salto de linea, respetando el número máximo de palabras de 70.
        """;

    context = StringUtils.replaceKeysWithValues(context, params);

    return conclusionOf(context);
  }

  public String integralNivelBienestar(Map<String, Object> params) {
    String context = """
        [nse_top1y2_poblacion_porcentaje]% de la población tienen esta preponderancia de nivel de bienestar dentro del polígono de 15 minutos caminando.
        Con el [nse_top1_poblacion_porcentaje]% de la población que pertenece al nivel socio-económico [nse_top_1_nse] que pertenece a [nse_top_1_nombre] y tiene un ingreso mensual de aproximadamente [nse_top_1_desc].
        Y el [nse_top2_poblacion_porcentaje]% de la población que pertenece al nivel socio-económico [nse_top_2_nse] que pertenece a [nse_top_2_nombre] y tiene un ingreso mensual de aproximadamente [nse_top_2_desc].
        """;

    context = StringUtils.replaceKeysWithValues(context, params);

    return conclusionOf(context);
  }

  private String conclusionOf(String context) {
    CompletionRequest request = new CompletionRequest();
    request.setMessages(List.of(new Message(MessageRole.system.name(), config.getCompletionsSystemMessage()),
        new Message(MessageRole.user.name(), context)));
    return completionService.generateCompletion(request);
  }
}
