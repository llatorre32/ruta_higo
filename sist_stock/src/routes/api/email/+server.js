import { json } from '@sveltejs/kit';

export async function POST({ request }) {
  try {
    const { para, asunto, mensaje, ventaId } = await request.json();

    // Validaciones básicas
    if (!para || !para.includes('@')) {
      return json({ error: 'Email inválido' }, { status: 400 });
    }

    if (!asunto || !mensaje) {
      return json({ error: 'Asunto y mensaje son obligatorios' }, { status: 400 });
    }

    console.log('Procesando envío de email:', {
      para,
      asunto,
      mensaje: mensaje.substring(0, 100) + '...',
      ventaId
    });

    // Intentar enviar email real
    try {
      // Importación dinámica de nodemailer para manejar casos donde no esté instalado
      const nodemailerModule = await import('nodemailer');
      const nodemailer = nodemailerModule.default;
      
      const transporter = nodemailer.createTransport({
        service: "gmail",
        auth: {
          user: "higoscatamarca1@gmail.com",
          pass: "nzvs daqj hlyo zofc"
        },
      });

      const info = await transporter.sendMail({
        from: "higoscatamarca1@gmail.com",
        to: para,
        subject: asunto,
        text: mensaje,
        html: mensaje.replace(/\n/g, '<br>')
      });

      console.log('Email enviado exitosamente:', info.messageId);

      return json({
        success: true,
        message: 'Email enviado correctamente',
        messageId: info.messageId
      });

    } catch (emailError) {
      console.error('Error al enviar email:', emailError);
      
      // Si nodemailer no está disponible o hay problemas, simular envío
      if (emailError.code === 'ERR_MODULE_NOT_FOUND' || 
          emailError.message?.includes('Cannot resolve module') ||
          emailError.message?.includes('nodemailer')) {
        
        console.log('Problema con nodemailer, simulando envío...');
        
        // Simular delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        return json({
          success: true,
          message: 'Email simulado correctamente',
          simulated: true,
          debug: { para, asunto, error: emailError.message }
        });
      }
      
      // Re-lanzar otros errores para ser capturados por el catch principal
      throw emailError;
    }

  } catch (error) {
    console.error('Error general en API de email:', error);
    return json(
      { 
        error: 'Error interno del servidor al enviar email',
        details: error.message,
        timestamp: new Date().toISOString()
      },
      { status: 500 }
    );
  }
}