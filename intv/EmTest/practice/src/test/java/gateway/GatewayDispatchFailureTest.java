package gateway;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import context.ModelContext;
import java.util.Random;
import llm.LLMProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tracking.STATUS;

@ExtendWith(MockitoExtension.class)
class GatewayDispatchFailureTest {

    @Mock
    private LLMProvider claude;

    @Mock
    private LLMProvider openAi;

    @Mock
    private ModelContext claudeCtx;

    @Mock
    private ModelContext openAiCtx;

    @Mock
    private Random random;

    @BeforeEach
    void wireContexts() {
        when(claude.modelContext()).thenReturn(claudeCtx);
        when(openAi.modelContext()).thenReturn(openAiCtx);
    }

    @Test
    void providerThrows_recordsFailure_andPropagates() {
        when(claudeCtx.isUp()).thenReturn(true);
        when(claude.askPrompt("p")).thenThrow(new IllegalStateException("upstream error"));

        Gateway gateway = new Gateway(claude, openAi, random);

        assertThrows(IllegalStateException.class, () -> gateway.askPrompt("p"));

        verify(claudeCtx).onResponse(STATUS.FAILURE);
        verify(claudeCtx, org.mockito.Mockito.never()).onResponse(STATUS.SUCCESS);
    }
}
