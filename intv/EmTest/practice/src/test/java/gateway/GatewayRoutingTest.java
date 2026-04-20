package gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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
class GatewayRoutingTest {

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
        lenient().when(claude.modelContext()).thenReturn(claudeCtx);
        lenient().when(openAi.modelContext()).thenReturn(openAiCtx);
    }

    @Test
    void claudeUp_sendsAllTrafficToClaude_andDoesNotRollDice() {
        when(claudeCtx.isUp()).thenReturn(true);
        when(claude.askPrompt("p")).thenReturn("from-claude");

        Gateway gateway = new Gateway(claude, openAi, random);
        assertEquals("from-claude", gateway.askPrompt("p"));

        verify(claude).askPrompt("p");
        verify(claudeCtx).onResponse(STATUS.SUCCESS);
        verify(openAi, never()).askPrompt(anyString());
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void claudeDown_openAiUp_draw0_to5_percentHitsClaude() {
        when(claudeCtx.isUp()).thenReturn(false);
        when(openAiCtx.isUp()).thenReturn(true);
        when(random.nextInt(100)).thenReturn(0);
        when(claude.askPrompt("p")).thenReturn("claude-rare");

        Gateway gateway = new Gateway(claude, openAi, random);
        assertEquals("claude-rare", gateway.askPrompt("p"));

        verify(claude).askPrompt("p");
        verify(claudeCtx).onResponse(STATUS.SUCCESS);
        verify(openAi, never()).askPrompt(anyString());
    }

    @Test
    void claudeDown_openAiUp_draw5_hitsOpenAi() {
        when(claudeCtx.isUp()).thenReturn(false);
        when(openAiCtx.isUp()).thenReturn(true);
        when(random.nextInt(100)).thenReturn(5);
        when(openAi.askPrompt("p")).thenReturn("from-openai");

        Gateway gateway = new Gateway(claude, openAi, random);
        assertEquals("from-openai", gateway.askPrompt("p"));

        verify(openAi).askPrompt("p");
        verify(openAiCtx).onResponse(STATUS.SUCCESS);
        verify(claude, never()).askPrompt(anyString());
    }

    @Test
    void bothDown_draw4_hitsClaude() {
        when(claudeCtx.isUp()).thenReturn(false);
        when(openAiCtx.isUp()).thenReturn(false);
        when(random.nextInt(100)).thenReturn(4);
        when(claude.askPrompt("p")).thenReturn("c");

        Gateway gateway = new Gateway(claude, openAi, random);
        assertEquals("c", gateway.askPrompt("p"));

        verify(claude).askPrompt("p");
        verify(claudeCtx).onResponse(STATUS.SUCCESS);
    }

    @Test
    void bothDown_draw9_hitsOpenAi() {
        when(claudeCtx.isUp()).thenReturn(false);
        when(openAiCtx.isUp()).thenReturn(false);
        when(random.nextInt(100)).thenReturn(9);
        when(openAi.askPrompt("p")).thenReturn("o");

        Gateway gateway = new Gateway(claude, openAi, random);
        assertEquals("o", gateway.askPrompt("p"));

        verify(openAi).askPrompt("p");
        verify(openAiCtx).onResponse(STATUS.SUCCESS);
    }

    @Test
    void bothDown_draw10_dropsRequest() {
        when(claudeCtx.isUp()).thenReturn(false);
        when(openAiCtx.isUp()).thenReturn(false);
        when(random.nextInt(100)).thenReturn(10);

        Gateway gateway = new Gateway(claude, openAi, random);
        assertThrows(GatewayRejectedException.class, () -> gateway.askPrompt("p"));

        verify(claude, never()).askPrompt(anyString());
        verify(openAi, never()).askPrompt(anyString());
    }
}
