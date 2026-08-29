# Guía: Crear una vista con Thymeleaf + Tailwind + Vue

Guía paso a paso para crear una vista interactiva usando el stack completo.

## Estructura base

Toda vista sigue esta estructura:

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Titulo')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex flex-col items-center justify-center gap-4 p-4">
    <!-- Tu contenido aquí -->
  </main>

  <script>
    Vue.createApp({
      data() {
        return { /* estado reactivo */ }
      },
      methods: {
        /* funciones */
      }
    }).mount('#mi-app');
  </script>

</body>
</html>
```

## Ejemplo completo: Formulario de contacto

### 1. Crear el template

Crear `src/main/webapp/WEB-INF/templates/pages/contact.html`:

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Contacto')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex items-center justify-center p-4">
    <div id="contact-app" class="w-full max-w-md rounded-lg bg-white p-8 shadow-md">

      <form @submit.prevent="submit" action="#" th:action="@{/contact}" method="POST">
        <h3 class="mb-2 text-2xl font-semibold text-gray-900">Contacto</h3>
        <hr class="mb-6 border-gray-200">

        <!-- Campo nombre -->
        <div class="mb-4">
          <label for="name" class="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
          <input v-model="name" id="name" name="name" type="text"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
          <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
        </div>

        <!-- Campo email -->
        <div class="mb-4">
          <label for="email" class="mb-1 block text-sm font-medium text-gray-700">Email</label>
          <input v-model="email" id="email" name="email" type="email"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
          <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
        </div>

        <!-- Campo mensaje -->
        <div class="mb-4">
          <label for="message" class="mb-1 block text-sm font-medium text-gray-700">Mensaje</label>
          <textarea v-model="message" id="message" name="message" rows="4"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"></textarea>
          <p v-if="errors.message" class="mt-1 text-sm text-red-600">{{ errors.message }}</p>
        </div>

        <!-- Botón submit -->
        <button type="submit" :disabled="loading"
          class="w-full rounded-md bg-blue-600 py-2 font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50">
          <span v-if="loading">Enviando...</span>
          <span v-else>Enviar</span>
        </button>

        <!-- Error general -->
        <p v-if="error" class="mt-4 rounded-md bg-red-100 px-4 py-3 text-sm text-red-700">{{ error }}</p>

        <!-- Éxito -->
        <p v-if="success" class="mt-4 rounded-md bg-green-100 px-4 py-3 text-sm text-green-700">{{ success }}</p>
      </form>

    </div>
  </main>

  <script>
    Vue.createApp({
      data() {
        return {
          name: '',
          email: '',
          message: '',
          error: '',
          success: '',
          errors: {},
          loading: false
        }
      },
      methods: {
        async submit() {
          this.loading = true;
          this.error = '';
          this.success = '';
          this.errors = {};

          const form = this.$el.querySelector('form');
          const res = await fetch(form.action, {
            method: 'POST',
            body: new FormData(form)
          });

          if (res.redirected) {
            window.location.href = res.url;
          } else {
            const html = await res.text();
            const doc = new DOMParser().parseFromString(html, 'text/html');

            // Buscar error general
            const errorEl = doc.querySelector('.alert-danger');
            if (errorEl) {
              this.error = errorEl.textContent.trim();
            }

            // Buscar errores de campos
            const fieldErrors = doc.querySelectorAll('.alert-danger');
            fieldErrors.forEach(el => {
              if (el.previousElementSibling && el.previousElementSibling.name) {
                this.errors[el.previousElementSibling.name] = el.textContent.trim();
              }
            });
          }

          this.loading = false;
        }
      }
    }).mount('#contact-app');
  </script>

</body>
</html>
```

### 2. Crear el controller

```java
@Controller
public class ContactController {

  @GetMapping("/contact")
  public String showForm(Model model) {
    model.addAttribute("contactData", new ContactData());
    return "pages/contact";
  }

  @PostMapping("/contact")
  public String submit(@Valid @ModelAttribute ContactData data, BindingResult result) {
    if (result.hasErrors()) {
      return "pages/contact";
    }
    // Procesar el formulario
    return "redirect:/contact?success";
  }
}
```

### 3. Crear el DTO

```java
public class ContactData {

  @NotBlank(message = "Nombre es requerido")
  private String name;

  @NotBlank(message = "Email es requerido")
  @Email(message = "Email no es válido")
  private String email;

  @NotBlank(message = "Mensaje es requerido")
  private String message;

  // getters y setters
}
```

## Patrones comunes

### Loading state

```html
<button type="submit" :disabled="loading">
  <span v-if="loading">Guardando...</span>
  <span v-else>Guardar</span>
</button>
```

### Errores inline

```html
<p v-if="errors.email" class="text-sm text-red-600">{{ errors.email }}</p>
```

### Éxito

```html
<p v-if="success" class="rounded-md bg-green-100 px-4 py-3 text-sm text-green-700">
  {{ success }}
</p>
```

### Confirmación antes de acción

```html
<button @click="confirmDelete" class="text-red-600 hover:underline">Eliminar</button>

<!-- Modal de confirmación -->
<div v-if="showConfirm" class="fixed inset-0 flex items-center justify-center bg-black/50">
  <div class="rounded-lg bg-white p-6 shadow-md">
    <p>¿Estás seguro?</p>
    <div class="mt-4 flex gap-2">
      <button @click="showConfirm = false" class="rounded bg-gray-200 px-4 py-2">Cancelar</button>
      <button @click="doDelete" class="rounded bg-red-600 px-4 py-2 text-white">Eliminar</button>
    </div>
  </div>
</div>
```

### Listas reactivas

```html
<ul>
  <li v-for="item in items" :key="item.id" class="py-2">
    {{ item.name }}
    <button @click="removeItem(item.id)" class="ml-2 text-red-600">X</button>
  </li>
</ul>
```

## Reglas

1. **Cada página monta su propio Vue app** — no compartir entre páginas
2. **Thymeleaf renderiza datos iniciales** — Vue toma control después
3. **Siempre `th:action` + `method="POST"`** — funciona sin JS (progressive enhancement)
4. **`v-model` en todos los inputs** — binding reactivo
5. **`@submit.prevent`** — prevenir submit tradicional
6. **`:disabled="loading"`** — feedback al usuario
7. **`v-if="error"`** — mostrar errores inline
