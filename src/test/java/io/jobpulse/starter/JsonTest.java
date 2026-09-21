package io.jobpulse.starter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {

    @Test
    void escapesQuotesAndControlCharacters() {
        assertThat(Json.escape("say \"hi\"\nnew\tline"))
                .isEqualTo("say \\\"hi\\\"\\nnew\\tline");
    }

    @Test
    void nullBecomesEmptyString() {
        assertThat(Json.escape(null)).isEmpty();
    }
}
