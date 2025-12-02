<script>
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';

	export let data;
	$: user = data.user;

	let currentSection = 'resumen';
	
	onMount(() => {
		// Obtener sección actual de la URL
		const path = $page.url.pathname;
		if (path.includes('/productos')) currentSection = 'productos';
		else if (path.includes('/categorias')) currentSection = 'categorias';
		else if (path.includes('/movimientos')) currentSection = 'movimientos';
		else if (path.includes('/reportes')) currentSection = 'reportes';
		else if (path.includes('/usuarios')) currentSection = 'usuarios';
		else if (path.includes('/ventas')) currentSection = 'ventas';
		else if (path.includes('/clientes')) currentSection = 'clientes';
		else if (path.includes('/perfil')) currentSection = 'perfil';
		else currentSection = 'resumen';
	});

	async function logout() {
		try {
			await fetch('/api/auth/logout', { method: 'POST' });
			goto('/');
		} catch (error) {
			console.error('Error al cerrar sesión:', error);
			goto('/');
		}
	}

	function navigate(section) {
		currentSection = section;
		if (section === 'resumen') {
			goto('/dashboard');
		} else {
			goto(`/dashboard/${section}`);
		}
	}

	// Verificar permisos por rol
	function canAccess(section) {
		if (user.rol === 'administrador') return true;
		if (user.rol === 'manejador') {
			return ['resumen', 'productos', 'categorias', 'movimientos', 'reportes', 'ventas', 'clientes'].includes(section);
		}
		return false;
	}
</script>

<div class="dashboard-layout">
	<aside class="sidebar">
		<div class="sidebar-header">
			<h2>Sistema de Stock</h2>
			<div class="user-info">
				<p class="user-name">{user.nombre}</p>
				<p class="user-role">{user.rol}</p>
			</div>
		</div>

		<nav class="sidebar-nav">
			<button 
				class="nav-item {currentSection === 'resumen' ? 'active' : ''}"
				on:click={() => navigate('resumen')}
			>
				📊 Resumen
			</button>

			{#if canAccess('productos')}
			<button 
				class="nav-item {currentSection === 'productos' ? 'active' : ''}"
				on:click={() => navigate('productos')}
			>
				📦 Productos
			</button>
			{/if}

			{#if canAccess('categorias')}
			<button 
				class="nav-item {currentSection === 'categorias' ? 'active' : ''}"
				on:click={() => navigate('categorias')}
			>
				🏷️ Categorías
			</button>
			{/if}

			{#if canAccess('ventas')}
			<button 
				class="nav-item {currentSection === 'ventas' ? 'active' : ''}"
				on:click={() => navigate('ventas')}
			>
				🛒 Ventas
			</button>
			{/if}

			{#if canAccess('clientes')}
			<button 
				class="nav-item {currentSection === 'clientes' ? 'active' : ''}"
				on:click={() => navigate('clientes')}
			>
				👥 Clientes
			</button>
			{/if}

			{#if canAccess('movimientos')}
			<button 
				class="nav-item {currentSection === 'movimientos' ? 'active' : ''}"
				on:click={() => navigate('movimientos')}
			>
				📝 Movimientos
			</button>
			{/if}

			{#if canAccess('reportes')}
			<button 
				class="nav-item {currentSection === 'reportes' ? 'active' : ''}"
				on:click={() => navigate('reportes')}
			>
				📈 Reportes
			</button>
			{/if}

			{#if canAccess('usuarios')}
			<button 
				class="nav-item {currentSection === 'usuarios' ? 'active' : ''}"
				on:click={() => navigate('usuarios')}
			>
				⚙️ Usuarios
			</button>
			{/if}

			<!-- Opción de perfil para todos los usuarios -->
			<button 
				class="nav-item {currentSection === 'perfil' ? 'active' : ''}"
				on:click={() => navigate('perfil')}
			>
				👤 Mi Perfil
			</button>
		</nav>

		<div class="sidebar-footer">
			<button class="logout-btn" on:click={logout}>
				🚪 Cerrar Sesión
			</button>
		</div>
	</aside>

	<main class="main-content">
		<slot></slot>
	</main>
</div>

<style>
	.dashboard-layout {
		display: flex;
		min-height: 100vh;
		background-color: #f5f5f5;
	}

	.sidebar {
		width: 250px;
		background-color: #2d3748;
		color: white;
		display: flex;
		flex-direction: column;
		box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
	}

	.sidebar-header {
		padding: 1.5rem;
		border-bottom: 1px solid #4a5568;
	}

	.sidebar-header h2 {
		margin: 0 0 1rem 0;
		font-size: 1.5rem;
		color: #fff;
	}

	.user-info {
		background-color: #4a5568;
		padding: 0.75rem;
		border-radius: 5px;
	}

	.user-name {
		margin: 0;
		font-weight: 500;
		font-size: 0.9rem;
	}

	.user-role {
		margin: 0.25rem 0 0 0;
		font-size: 0.8rem;
		color: #a0aec0;
		text-transform: capitalize;
	}

	.sidebar-nav {
		flex: 1;
		padding: 1rem 0;
	}

	.nav-item {
		width: 100%;
		background: none;
		border: none;
		color: #e2e8f0;
		padding: 0.75rem 1.5rem;
		text-align: left;
		cursor: pointer;
		transition: background-color 0.2s;
		font-size: 0.9rem;
		display: block;
	}

	.nav-item:hover {
		background-color: #4a5568;
	}

	.nav-item.active {
		background-color: #667eea;
		color: white;
	}

	.sidebar-footer {
		padding: 1rem;
		border-top: 1px solid #4a5568;
	}

	.logout-btn {
		width: 100%;
		background-color: #e53e3e;
		color: white;
		border: none;
		padding: 0.75rem;
		border-radius: 5px;
		cursor: pointer;
		transition: background-color 0.2s;
		font-size: 0.9rem;
	}

	.logout-btn:hover {
		background-color: #c53030;
	}

	.main-content {
		flex: 1;
		padding: 2rem;
		overflow-y: auto;
	}

	@media (max-width: 768px) {
		.dashboard-layout {
			flex-direction: column;
		}
		
		.sidebar {
			width: 100%;
			height: auto;
		}
		
		.sidebar-nav {
			display: flex;
			overflow-x: auto;
			padding: 0.5rem;
		}
		
		.nav-item {
			white-space: nowrap;
			min-width: auto;
			padding: 0.5rem 1rem;
		}
		
		.main-content {
			padding: 1rem;
		}
	}
</style>