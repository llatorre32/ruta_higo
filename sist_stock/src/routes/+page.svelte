<script>
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';

	let username = '';
	let password = '';
	let error = '';
	let loading = false;

	// Redirigir al dashboard si ya está autenticado
	onMount(() => {
		if ($page.data.user) {
			goto('/dashboard');
		}
	});

	async function handleLogin() {
		if (!username || !password) {
			error = 'Por favor complete todos los campos';
			return;
		}

		loading = true;
		error = '';

		try {
			const response = await fetch('/api/auth/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ username, password })
			});

			const data = await response.json();

			if (response.ok) {
				// Login exitoso, redirigir al dashboard
				goto('/dashboard');
			} else {
				error = data.error || 'Error al iniciar sesión';
			}
		} catch (err) {
			error = 'Error de conexión';
		} finally {
			loading = false;
		}
	}
</script>

<svelte:head>
	<title>Sistema de Stock - Iniciar Sesión</title>
	<meta name="description" content="Sistema de gestión de stock - Iniciar sesión" />
</svelte:head>

<main class="login-container">
	<div class="login-card">
		<div class="login-header">
			<h1>Sistema de Stock</h1>
			<p>Iniciar Sesión</p>
		</div>

		<form on:submit|preventDefault={handleLogin}>
			<div class="form-group">
				<label for="username">Usuario</label>
				<input
					type="text"
					id="username"
					bind:value={username}
					required
					disabled={loading}
					placeholder="Ingrese su usuario"
				/>
			</div>

			<div class="form-group">
				<label for="password">Contraseña</label>
				<input
					type="password"
					id="password"
					bind:value={password}
					required
					disabled={loading}
					placeholder="Ingrese su contraseña"
				/>
			</div>

			{#if error}
				<div class="error">{error}</div>
			{/if}

			<button type="submit" disabled={loading}>
				{loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
			</button>
		</form>

		<div class="demo-info">
			<h3>Usuarios de demostración:</h3>
			<p><strong>Administrador:</strong> admin / admin123</p>
			<p><strong>Manejador:</strong> manager / manager123</p>
		</div>
	</div>
</main>

<style>
	.login-container {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		padding: 1rem;
	}

	.login-card {
		background: white;
		padding: 2rem;
		border-radius: 10px;
		box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
		width: 100%;
		max-width: 400px;
	}

	.login-header {
		text-align: center;
		margin-bottom: 2rem;
	}

	.login-header h1 {
		color: #333;
		margin: 0 0 0.5rem 0;
		font-size: 2rem;
	}

	.login-header p {
		color: #666;
		margin: 0;
		font-size: 1.1rem;
	}

	.form-group {
		margin-bottom: 1.5rem;
	}

	.form-group label {
		display: block;
		margin-bottom: 0.5rem;
		color: #333;
		font-weight: 500;
	}

	.form-group input {
		width: 100%;
		padding: 0.75rem;
		border: 2px solid #ddd;
		border-radius: 5px;
		font-size: 1rem;
		transition: border-color 0.3s;
		box-sizing: border-box;
	}

	.form-group input:focus {
		outline: none;
		border-color: #667eea;
	}

	.form-group input:disabled {
		background-color: #f5f5f5;
		cursor: not-allowed;
	}

	.error {
		background-color: #fee;
		color: #c33;
		padding: 0.75rem;
		border-radius: 5px;
		margin-bottom: 1rem;
		border-left: 4px solid #c33;
	}

	button {
		width: 100%;
		background: #667eea;
		color: white;
		border: none;
		padding: 0.75rem;
		border-radius: 5px;
		font-size: 1rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.3s;
		margin-bottom: 1.5rem;
	}

	button:hover:not(:disabled) {
		background: #5a67d8;
	}

	button:disabled {
		background: #ccc;
		cursor: not-allowed;
	}

	.demo-info {
		border-top: 1px solid #eee;
		padding-top: 1rem;
		text-align: center;
	}

	.demo-info h3 {
		color: #333;
		margin: 0 0 0.5rem 0;
		font-size: 0.9rem;
	}

	.demo-info p {
		color: #666;
		margin: 0.25rem 0;
		font-size: 0.8rem;
	}
</style>
