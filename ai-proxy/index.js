import express from "express";
import { z } from "zod";
import { ChatMistralAI } from "@langchain/mistralai";
import https from "https";

// Ignore SSL certificates for corporate environment
process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = "0";

// LangSmith: activer le tracing en passant les env dans Docker/host:
// LANGCHAIN_TRACING_V2=true
// LANGCHAIN_API_KEY=lsm_xxx
// LANGCHAIN_PROJECT=ton_projet
// Optionnel: LANGCHAIN_ENDPOINT=https://api.smith.langchain.com

const app = express();
app.use(express.json({ limit: "1mb" }));

const bodySchema = z.object({
  title: z.string().default(""),
  content: z.string().default("")
});

// Function to sanitize content for safe string interpolation
function sanitizeForTemplate(content) {
  if (!content) return '';
  
  try {
    return String(content)
      .replace(/\\/g, '\\\\')  // Escape backslashes
      .replace(/`/g, '\\`')    // Escape backticks
      .replace(/\$/g, '\\$')   // Escape dollar signs
      .replace(/\n/g, '\\n')   // Escape newlines
      .replace(/\r/g, '\\r')   // Escape carriage returns
      .replace(/\t/g, '\\t')   // Escape tabs
      .replace(/\f/g, '\\f')   // Escape form feeds
      .replace(/\v/g, '\\v')   // Escape vertical tabs
      .replace(/\0/g, '\\0')   // Escape null characters
      .replace(/[\x00-\x1F\x7F]/g, ''); // Remove other control characters
  } catch (error) {
    console.warn("Error sanitizing content, using fallback:", error);
    // Fallback: just remove problematic characters
    return String(content || '')
      .replace(/[\x00-\x1F\x7F]/g, '') // Remove control characters
      .replace(/[`$\\]/g, ''); // Remove template-breaking characters
  }
}

app.post("/score", async (req, res) => {
  try {
    const { title, content } = bodySchema.parse(req.body);
    console.log(req.body);
    console.log("Received request - Title length:", title?.length || 0, "Content length:", content?.length || 0);
    
    const modelName = process.env.MISTRAL_MODEL || "mistral-large-latest";

    const model = new ChatMistralAI({
      apiKey: process.env.MISTRAL_API_KEY,
      model: modelName,
      temperature: 0.2,
      maxTokens: 50
    });

    const sys = "Tu es un expert One Piece qui évalue la crédibilité des théories. Réponds UNIQUEMENT au format JSON: {\"score\": X} où X est un entier entre 0 et 100. Si le contenu de la théorie contient un discours haineux, de la pub déguisée et/ou est à propos d'un sujet qui n'est pas One Piece, réponds -200.";
    
    // Sanitize content before using in template string
    const sanitizedTitle = sanitizeForTemplate(title).slice(0, 500);
    const sanitizedContent = sanitizeForTemplate(content).slice(0, 3000);
    
    console.log("Sanitized title length:", sanitizedTitle.length, "Sanitized content length:", sanitizedContent.length);
    
    let user;
    try {
      // Use string concatenation instead of template literals for extra safety
      user = "Évalue cette théorie One Piece et donne un score de crédibilité (0-100).\n\n" +
             "Titre: " + sanitizedTitle + "\n" +
             "Contenu:\n" + sanitizedContent + "\n\n" +
             "Réponds UNIQUEMENT au format JSON: {\"score\": X}. Si le contenu de la théorie contient un discours haineux, de la pub déguisée et/ou est à propos d'un sujet qui n'est pas One Piece, réponds -200.";
    } catch (templateError) {
      console.error("Error creating prompt string:", templateError);
      throw new Error("Failed to create prompt template");
    }

    const resp = await model.invoke([
      { role: "system", content: sys },
      { role: "user", content: user }
    ]);

    const text = String(resp?.content ?? "").trim();
    
    try {
      // Essayer de parser le JSON
      const jsonMatch = text.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        const parsed = JSON.parse(jsonMatch[0]);
        console.log("Parsed AI response:", parsed);
        console.log("Raw score from AI:", parsed.score);
        const score = parsed.score === -200 ? -200 : Math.max(-110, Math.min(100, parseInt(parsed.score !== undefined ? parsed.score : "-110", 10)));
        console.log("Processed score:", score);
        console.log("Is finite:", Number.isFinite(score));
        res.json({ 
          status: 200, 
          aiScore: Number.isFinite(score) ? score : -110, 
          justification: "Score basé sur l'évaluation de l'IA",
          raw: text 
        });
      } else {
        // Fallback: extraire juste les chiffres comme avant
        const digits = text.replace(/\D+/g, "");
        const n = Math.max(-110, Math.min(100, parseInt(digits !== "" ? digits : "-110", 10)));
        res.json({ 
          status: 200, 
          aiScore: Number.isFinite(n) ? n : -110, 
          justification: "Format de réponse non reconnu",
          raw: text 
        });
      }
    } catch (e) {
      // Fallback en cas d'erreur de parsing JSON
      const digits = text.replace(/\D+/g, "");
      const n = Math.max(-110, Math.min(100, parseInt(digits !== "" ? digits : "-110", 10)));
      res.json({ 
        status: 200, 
        aiScore: Number.isFinite(n) ? n : -110, 
        justification: "Erreur de parsing de la réponse",
        raw: text 
      });
    }
  } catch (e) {
    console.error("Error in /score endpoint:", e);
    res.status(200).json({ 
      status: 200, 
      aiScore: -110, 
      error: String(e?.message || e),
      errorType: e?.constructor?.name || 'Unknown'
    });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`ai-proxy listening on ${port}`));