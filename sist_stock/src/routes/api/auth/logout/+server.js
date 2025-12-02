import { logout } from '$lib/auth.js';
import { json, redirect } from '@sveltejs/kit';

export async function POST({ cookies }) {
  const token = cookies.get('session_token');
  
  if (token) {
    logout(token);
  }
  
  cookies.delete('session_token', { path: '/' });
  
  return json({ success: true });
}