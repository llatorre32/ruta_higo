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

    // Aquí iría la lógica real de envío de email
    // Por ejemplo, usando nodemailer, SendGrid, etc.
    
    // Simulación de envío exitoso
    console.log('Email enviado:', {
      para,
      asunto,
      mensaje: mensaje.substring(0, 100) + '...',
      ventaId
    });

    // En una implementación real, aquí enviarías el email:
    /*
    const nodemailer = require('nodemailer');
    
    const transporter = nodemailer.createTransporter({
      host: process.env.SMTP_HOST,
      port: process.env.SMTP_PORT,
      secure: false,
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS
      }
    });

    const info = await transporter.sendMail({
      from: process.env.FROM_EMAIL,
      to: para,
      subject: asunto,
      text: mensaje,
      html: mensaje.replace(/\n/g, '<br>')
    });

    if (!info.messageId) {
      throw new Error('Error al enviar email');
    }
    */

    // Simular un pequeño delay para mostrar el estado de "enviando"
    await new Promise(resolve => setTimeout(resolve, 1500));

    return json({ 
      success: true, 
      message: 'Email enviado correctamente',
      // En desarrollo, mostramos que es una simulación
      debug: 'Email simulado en desarrollo - configurar SMTP real en producción'
    });

  } catch (error) {
    console.error('Error al enviar email:', error);
    return json(
      { error: 'Error interno del servidor al enviar email' }, 
      { status: 500 }
    );
  }
}