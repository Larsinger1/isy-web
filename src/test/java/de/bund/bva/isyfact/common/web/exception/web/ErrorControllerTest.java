package de.bund.bva.isyfact.common.web.exception.web;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import de.bund.bva.isyfact.common.TestFieldSetter;
import de.bund.bva.isyfact.common.web.MockFacesContext;
import de.bund.bva.isyfact.common.web.exception.common.AusnahmeIdMapper;
import de.bund.bva.isyfact.common.web.exception.common.FehlerInformation;
import de.bund.bva.isyfact.common.web.konstanten.FehlerSchluessel;
import de.bund.bva.pliscommon.exception.FehlertextProvider;
import de.bund.bva.pliscommon.exception.PlisException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ErrorControllerTest {

    private static final String REQUEST_PARAMETER_UNIQUE_ID = "uniqueId";

    private static final String UNIQUE_ID = "testUniqueId";

    private static final String REQUEST_PARAMETER_FEHLER_ID = "fehlerId";

    private static final String FEHLER_ID = "testFehlerId";

    private static final String ERGEBNIS_NACHRICHT = "testFehlerNachricht";

    private static final String ERGEBNIS_UEBERSCHRIFT = "testFehlerUeberschrift";

    private Map<String, String> requestParameterMap = new HashMap<>();

    private FehlertextProvider fehlertextProviderMock;

    @Before
    public void setupFehlertextProvider() throws Exception {
        fehlertextProviderMock = mock(FehlertextProvider.class);

        TestFieldSetter.setFinalStatic(FehlerInformation.class.getDeclaredField("FEHLERTEXT_PROVIDER"),
            fehlertextProviderMock);

        when(fehlertextProviderMock
            .getMessage(FehlerSchluessel.FEHLERTEXT_GUI_TECHNISCH, FEHLER_ID, UNIQUE_ID))
            .thenReturn(ERGEBNIS_NACHRICHT);
        when(fehlertextProviderMock
            .getMessage(FehlerSchluessel.FEHLERTEXT_GUI_TECHNISCH_UEBERSCHRIFT, FEHLER_ID, UNIQUE_ID))
            .thenReturn(ERGEBNIS_UEBERSCHRIFT);
        when(fehlertextProviderMock.getMessage(FEHLER_ID)).thenReturn(ERGEBNIS_NACHRICHT);
    }


    @Before
    public void setupRequestParameterMap() {
        FacesContext context = MockFacesContext.mockFacesContext();

        requestParameterMap.clear();
        ExternalContext externalContext = mock(ExternalContext.class);

        when(externalContext.getRequestParameterMap()).thenReturn(requestParameterMap);
        when(context.getExternalContext()).thenReturn(externalContext);
    }

    @Test
    public void initialisiereModel() {
        requestParameterMap.put(REQUEST_PARAMETER_UNIQUE_ID, UNIQUE_ID);
        requestParameterMap.put(REQUEST_PARAMETER_FEHLER_ID, FEHLER_ID);

        ErrorModel model = new ErrorModel();

        ErrorController errorController = new ErrorController();

        errorController.initialisiereModel(model, null);

        assertEquals(ERGEBNIS_NACHRICHT, model.getFehlerText());
        assertEquals(ERGEBNIS_UEBERSCHRIFT, model.getFehlerUeberschrift());
    }

    @Test
    public void initialisiereModelKeinFehlerImRequest() {

        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        AusnahmeIdMapper ausnahmeIdMapperMock = mock(AusnahmeIdMapper.class);

        PlisException exception = mock(PlisException.class);

        when(exception.getAusnahmeId()).thenReturn(FEHLER_ID);
        when(exception.getFehlertext()).thenReturn(ERGEBNIS_NACHRICHT);
        when(exception.getUniqueId()).thenReturn(UNIQUE_ID);

        when(applicationContextMock.getBean(AusnahmeIdMapper.class)).thenReturn(ausnahmeIdMapperMock);

        ErrorModel model = new ErrorModel();

        ErrorController errorController = new ErrorController();

        errorController.setApplicationContext(applicationContextMock);

        errorController.initialisiereModel(model, exception);

        assertEquals(ERGEBNIS_NACHRICHT, model.getFehlerText());
        assertEquals(ERGEBNIS_UEBERSCHRIFT, model.getFehlerUeberschrift());
    }

    @Test
    public void initialisiereModelSocketException() throws Exception {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        AusnahmeIdMapper ausnahmeIdMapperMock = mock(AusnahmeIdMapper.class);

        SocketException socketException = new SocketException();

        when(applicationContextMock.getBean(AusnahmeIdMapper.class)).thenReturn(ausnahmeIdMapperMock);
        when(ausnahmeIdMapperMock.getFehlertextProvider()).thenReturn(fehlertextProviderMock);
        when(ausnahmeIdMapperMock.getFallbackAusnahmeId()).thenReturn(FEHLER_ID);

        // Zufällige UUID wird generiert
        when(fehlertextProviderMock
            .getMessage(eq(FehlerSchluessel.FEHLERTEXT_GUI_TECHNISCH), eq(FEHLER_ID), any(String.class)))
            .thenReturn(ERGEBNIS_NACHRICHT);
        when(fehlertextProviderMock
            .getMessage(eq(FehlerSchluessel.FEHLERTEXT_GUI_TECHNISCH_UEBERSCHRIFT), eq(FEHLER_ID),
                any(String.class))).thenReturn(ERGEBNIS_UEBERSCHRIFT);

        ErrorModel model = new ErrorModel();

        ErrorController errorController = new ErrorController();

        errorController.setApplicationContext(applicationContextMock);

        errorController.initialisiereModel(model, socketException);

        assertEquals(ERGEBNIS_NACHRICHT, model.getFehlerText());
        assertEquals(ERGEBNIS_UEBERSCHRIFT, model.getFehlerUeberschrift());
    }
}