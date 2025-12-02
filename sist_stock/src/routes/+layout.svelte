<script>
	import Notifications from '$lib/components/Notifications.svelte';
	import StatusIndicator from '$lib/components/StatusIndicator.svelte';
	
	/** @type {{children: import('svelte').Snippet}} */
	let { children } = $props();
</script>

<div style="display: contents">
	{@render children()}
</div>

<!-- Componente de notificaciones global -->
<Notifications />

<!-- Indicador de estado de conexión -->
<StatusIndicator />

<style>
	:global(body) {
		margin: 0;
		padding: 0;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
			'Ubuntu', 'Cantarell', 'Open Sans', 'Helvetica Neue', sans-serif;
		background-color: #f5f5f5;
	}

	:global(*) {
		box-sizing: border-box;
	}
</style>
