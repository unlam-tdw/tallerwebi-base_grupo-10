# Guía: Crear una vista con Thymeleaf + CSS + JS vanilla

Guía paso a paso para crear una vista sin frameworks, usando solo HTML + CSS + JavaScript.

## Estructura base

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Titulo')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex items-center justify-center p-4">
    <!-- Tu contenido aquí -->
  </main>

  <script>
    // Tu JavaScript aquí
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
    <div id="contact-form-area" class="w-full max-w-md rounded-lg bg-white p-8 shadow-md">

      <form id="contact-form" action="#"
        th:action="@{/contact}"
        method="POST"
        th:object="${contactData}">

        <h3 class="mb-2 text-2xl font-semibold text-gray-900">Contacto</h3>
        <hr class="mb-6 border-gray-200">

        <!-- Campo nombre -->
        <div class="mb-4">
          <label for="name" class="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
          <input th:field="*{name}" id="name" type="text"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
          <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}"
            class="mt-1 text-sm text-red-600"></p>
        </div>

        <!-- Campo email -->
        <div class="mb-4">
          <label for="email" class="mb-1 block text-sm font-medium text-gray-700">Email</label>
          <input th:field="*{email}" id="email" type="email"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
          <p th:if="${#fields.hasErrors('email')}" th:errors="*{email}"
            class="mt-1 text-sm text-red-600"></p>
        </div>

        <!-- Campo mensaje -->
        <div class="mb-4">
          <label for="message" class="mb-1 block text-sm font-medium text-gray-700">Mensaje</label>
          <textarea th:field="*{message}" id="message" rows="4"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"></textarea>
          <p th:if="${#fields.hasErrors('message')}" th:errors="*{message}"
            class="mt-1 text-sm text-red-600"></p>
        </div>

        <!-- Botón submit -->
        <button id="btn-submit" type="submit"
          class="w-full rounded-md bg-blue-600 py-2 font-medium text-white transition hover:bg-blue-700">
          Enviar
        </button>

        <!-- Error general -->
        <p th:if="${error}" class="mt-4 rounded-md bg-red-100 px-4 py-3 text-sm text-red-700"
          th:text="${error}"></p>
      </form>

    </div>
  </main>

  <script>
    document.getElementById('contact-form').addEventListener('submit', async function(e) {
      e.preventDefault();
      var form = e.target;
      var res = await fetch(form.action, {
        method: 'POST',
        body: new FormData(form)
      });
      if (res.redirected) {
        window.location.href = res.url;
      } else {
        document.getElementById('contact-form-area').innerHTML = await res.text();
      }
    });
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

### Formulario sin JS (tradicional)

```html
<!-- El form hace submit tradicional (reload de página) -->
<form th:action="@{/contact}" method="POST" th:object="${contactData}">
  <input th:field="*{name}" type="text" />
  <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></p>
  <button type="submit">Enviar</button>
</form>
```

### Formulario con JS (fetch, sin reload)

```javascript
document.getElementById('my-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  var form = e.target;
  var res = await fetch(form.action, {
    method: 'POST',
    body: new FormData(form)
  });
  if (res.redirected) {
    window.location.href = res.url;
  } else {
    document.getElementById('form-area').innerHTML = await res.text();
  }
});
```

### Errores con Thymeleaf

```html
<!-- Error de campo -->
<p th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="text-sm text-red-600"></p>

<!-- Error general -->
<p th:if="${error}" th:text="${error}" class="rounded-md bg-red-100 px-4 py-3 text-sm text-red-700"></p>
```

### Condicional con Thymeleaf

```html
<!-- Mostrar algo solo si hay usuario -->
<div th:if="${user != null}">
  <span th:text="${user.email}">email</span>
</div>

<!-- Mostrar algo solo si NO hay usuario -->
<div th:if="${user == null}">
  <a th:href="@{/login}">Iniciar sesión</a>
</div>
```

### Loop con Thymeleaf

```html
<ul>
  <li th:each="item : ${items}" class="py-2">
    <span th:text="${item.name}">Nombre</span>
  </li>
</ul>
```

## Cuándo usar JS vanilla vs Vue

| Situación | JS vanilla | Vue |
| :--- | :--- | :--- |
| Form simple (1-2 campos) | ✅ Mejor | ❌ Overkill |
| Form con loading state | ⚠️ Manual | ✅ reactivo |
| Errores inline | ⚠️ innerHTML | ✅ v-if |
| Modal de confirmación | ⚠️ DOM manual | ✅ v-if reactivo |
| Lista reactiva | ❌ Doloroso | ✅ v-for |
| Timer / clock | ✅ setInterval | ✅ data() |
| Sin frameworks permitidos | ✅ Única opción | ❌ No usar |

## Reglas

1. **Siempre `th:action` + `method="POST"`** — funciona sin JS
2. **`id` en el form y en el contenedor** — para JS y para Thymeleaf
3. **`th:field` para bindear** — Thymeleaf maneja los valores
4. **`th:if` para errores** — solo se muestran si existen
5. **`async/await` en fetch** — código limpio
6. **`res.redirected`** — seguir redirects del server
7. **`innerHTML` en error** — reemplazar solo el form area
