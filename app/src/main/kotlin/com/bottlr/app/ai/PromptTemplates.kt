package com.bottlr.app.ai

/**
 * Prompt templates for bottle recognition AI.
 */
object PromptTemplates {
    /**
     * Prompt for recognizing bottle details from an image.
     * Returns JSON format for easy parsing.
     */
    val BOTTLE_RECOGNITION = """
        Analyze this image of a liquor bottle and extract the following information.
        Look at the label, bottle shape, and any visible text.

        Extract these fields if visible:
        - name: The full product name (brand + product line)
        - distillery: The distillery or producer name
        - type: The spirit type (e.g., Bourbon, Scotch Whisky, Rye, Vodka, Rum, Gin, Tequila, Cognac, Brandy)
        - region: Geographic region or country of origin
        - abv: Alcohol by volume as a decimal (e.g., 0.40 for 40%)
        - age: Age statement in years (if shown)

        For each field, also indicate your confidence:
        - HIGH: Clearly visible and readable
        - MEDIUM: Partially visible or inferred
        - LOW: Guessed from context

        Respond ONLY with valid JSON in this exact format, no other text:
        {
            "name": "string or null",
            "distillery": "string or null",
            "type": "string or null",
            "region": "string or null",
            "abv": number or null,
            "age": number or null,
            "confidence": {
                "name": "HIGH|MEDIUM|LOW",
                "distillery": "HIGH|MEDIUM|LOW",
                "type": "HIGH|MEDIUM|LOW",
                "region": "HIGH|MEDIUM|LOW",
                "abv": "HIGH|MEDIUM|LOW",
                "age": "HIGH|MEDIUM|LOW"
            }
        }

        If you cannot identify any bottle in the image, respond with:
        {"error": "No bottle detected in image"}
    """.trimIndent()

    /**
     * Simpler prompt for on-device models with limited context windows.
     */
    val BOTTLE_RECOGNITION_COMPACT = """
        Identify this liquor bottle. Return JSON only:
        {"name":"...", "distillery":"...", "type":"...", "region":"...", "abv":0.0, "age":0}
        Use null for unknown fields. No other text.
    """.trimIndent()
}
